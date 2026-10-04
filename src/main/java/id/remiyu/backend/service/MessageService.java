package id.remiyu.backend.service;

import id.remiyu.backend.dto.response.SourceMessageView;
import id.remiyu.backend.repository.MessageRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
  private final MessageRepository repository;
  private final WorkspaceService workspaces;

  public MessageService(MessageRepository repository, WorkspaceService workspaces) {
    this.repository = repository;
    this.workspaces = workspaces;
  }

  public List<SourceMessageView> sources(UUID user, UUID workspace, List<UUID> ids) {
    workspaces.requireMember(workspace, user);
    return repository.sources(workspace, ids);
  }
}
