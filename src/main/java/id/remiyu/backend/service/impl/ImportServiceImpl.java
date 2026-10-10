package id.remiyu.backend.service.impl;

import id.remiyu.backend.common.util.Hashes;
import id.remiyu.backend.dto.ai.AiContracts;
import id.remiyu.backend.dto.response.ImportResultResponse;
import id.remiyu.backend.dto.response.ImportRunView;
import id.remiyu.backend.integration.ai.AiGateway;
import id.remiyu.backend.model.enums.ImportStatus;
import id.remiyu.backend.model.enums.TaskStatus;
import id.remiyu.backend.repository.ImportRepository;
import id.remiyu.backend.repository.TaskRepository;
import id.remiyu.backend.service.ImportService;
import id.remiyu.backend.service.WorkspaceService;
import id.remiyu.backend.support.importchat.ImportFileReader;
import id.remiyu.backend.support.importchat.WhatsAppParser;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImportServiceImpl implements ImportService {
  private final WorkspaceService workspaces;
  private final WhatsAppParser parser;
  private final ImportFileReader reader;
  private final ImportRepository repository;
  private final TaskRepository tasks;
  private final AiGateway ai;
  private final String provider;

  public ImportServiceImpl(
      WorkspaceService workspaces,
      WhatsAppParser parser,
      ImportFileReader reader,
      ImportRepository repository,
      TaskRepository tasks,
      AiGateway ai,
      @Value("${remiyu.ai.provider}") String provider) {
    this.workspaces = workspaces;
    this.parser = parser;
    this.reader = reader;
    this.repository = repository;
    this.tasks = tasks;
    this.ai = ai;
    this.provider = provider;
  }

  @Override
  @Transactional
  public ImportResultResponse upload(UUID user, UUID workspace, MultipartFile file) {
    workspaces.requireAdmin(workspace, user);
    var parsed = parser.parse(reader.read(file));
    repository.lockWorkspace(workspace);
    var messages = new ArrayList<AiContracts.Message>();
    int duplicates = 0;
    for (var message : parsed) {
      String key =
          Hashes.sha256(
              message.sentAt().toInstant()
                  + "\u001f"
                  + message.sender()
                  + "\u001f"
                  + message.text());
      if (repository.contains(workspace, key)) {
        duplicates++;
        continue;
      }
      UUID id = repository.message(workspace, key, message);
      messages.add(new AiContracts.Message(id, message.sender(), message.sentAt(), message.text()));
    }
    UUID importId = UUID.randomUUID();
    String filename = file.getOriginalFilename() == null ? "chat.txt" : file.getOriginalFilename();
    filename = filename.replace('\\', '/');
    filename = filename.substring(filename.lastIndexOf('/') + 1);
    if (filename.length() > 255) filename = filename.substring(filename.length() - 255);
    repository.run(importId, workspace, filename, messages.size(), duplicates, provider);
    var taskIds = new ArrayList<UUID>();
    var agendaIds = new ArrayList<UUID>();
    if (!messages.isEmpty()) {
      var result =
          ai.extract(new AiContracts.ExtractionRequest(importId, workspace, user, messages));
      result
          .tasks()
          .forEach(
              t ->
                  taskIds.add(
                      tasks.create(
                          workspace,
                          importId,
                          t.title(),
                          t.description(),
                          t.assigneeName(),
                          t.dueAt(),
                          t.priority(),
                          TaskStatus.DRAFT,
                          t.sourceMessageIds())));
      result.agendas().forEach(a -> agendaIds.add(repository.agenda(workspace, importId, a)));
    }
    return new ImportResultResponse(
        importId,
        messages.size(),
        duplicates,
        taskIds,
        agendaIds,
        messages.isEmpty() ? ImportStatus.NO_NEW_MESSAGES : ImportStatus.NEEDS_REVIEW,
        provider);
  }

  @Override
  public List<ImportRunView> list(UUID user, UUID workspace) {
    workspaces.requireMember(workspace, user);
    return repository.list(workspace);
  }
}
