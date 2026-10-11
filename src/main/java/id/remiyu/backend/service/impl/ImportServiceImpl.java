package id.remiyu.backend.service.impl;

import id.remiyu.backend.common.util.Hashes;
import id.remiyu.backend.dto.ai.AiContracts;
import id.remiyu.backend.dto.response.ImportResultResponse;
import id.remiyu.backend.dto.response.ImportRunView;
import id.remiyu.backend.integration.ai.AiGateway;
import id.remiyu.backend.model.enums.ImportStatus;
import id.remiyu.backend.model.enums.TaskStatus;
import id.remiyu.backend.repository.AgendaRepository;
import id.remiyu.backend.repository.ImportRepository;
import id.remiyu.backend.repository.TaskRepository;
import id.remiyu.backend.service.ChannelService;
import id.remiyu.backend.service.ImportService;
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
  private final ChannelService channels;
  private final WhatsAppParser parser;
  private final ImportFileReader reader;
  private final ImportRepository repository;
  private final TaskRepository tasks;
  private final AgendaRepository agendas;
  private final AiGateway ai;
  private final String provider;

  public ImportServiceImpl(
      ChannelService channels,
      WhatsAppParser parser,
      ImportFileReader reader,
      ImportRepository repository,
      TaskRepository tasks,
      AgendaRepository agendas,
      AiGateway ai,
      @Value("${remiyu.ai.provider}") String provider) {
    this.channels = channels;
    this.parser = parser;
    this.reader = reader;
    this.repository = repository;
    this.tasks = tasks;
    this.agendas = agendas;
    this.ai = ai;
    this.provider = provider;
  }

  @Override
  @Transactional
  public ImportResultResponse upload(UUID user, UUID channel, MultipartFile file) {
    channels.requireAdmin(channel, user);
    var parsed = parser.parse(reader.read(file));
    repository.lockChannel(channel);
    UUID importId = UUID.randomUUID();
    String filename = file.getOriginalFilename() == null ? "chat.txt" : file.getOriginalFilename();
    filename = filename.replace('\\', '/');
    filename = filename.substring(filename.lastIndexOf('/') + 1);
    if (filename.length() > 255) filename = filename.substring(filename.length() - 255);
    repository.startRun(importId, channel, filename, provider);
    var messages = new ArrayList<AiContracts.Message>();
    int duplicates = 0;
    long messageOrder = repository.nextMessageOrder(channel);
    for (var message : parsed) {
      String normalizedText = message.text().trim().replaceAll("\\s+", " ");
      String hash =
          Hashes.sha256(
              channel
                  + "\u001f"
                  + messageOrder
                  + "\u001f"
                  + message.sender()
                  + "\u001f"
                  + normalizedText);
      if (repository.contains(channel, hash)) {
        duplicates++;
        messageOrder++;
        continue;
      }
      UUID id = repository.message(channel, importId, messageOrder, hash, message);
      messages.add(new AiContracts.Message(id, message.sender(), message.sentAt(), message.text()));
      messageOrder++;
    }
    var taskIds = new ArrayList<UUID>();
    var agendaIds = new ArrayList<UUID>();
    if (!messages.isEmpty()) {
      var result = ai.extract(new AiContracts.ExtractionRequest(importId, channel, user, messages));
      result
          .tasks()
          .forEach(
              t ->
                  taskIds.add(
                      tasks.create(
                          channel,
                          importId,
                          t.title(),
                          t.description(),
                          t.assigneeName(),
                          t.dueAt(),
                          t.priority(),
                          TaskStatus.DRAFT,
                          t.sourceMessageIds())));
      result
          .agendas()
          .forEach(
              a ->
                  agendaIds.add(
                      agendas.create(
                          channel, importId, a.title(), a.startsAt(), a.sourceMessageIds())));
    }
    repository.completeRun(importId, messages.size(), duplicates);
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
  public List<ImportRunView> list(UUID user, UUID channel) {
    channels.requireMember(channel, user);
    return repository.list(channel);
  }
}
