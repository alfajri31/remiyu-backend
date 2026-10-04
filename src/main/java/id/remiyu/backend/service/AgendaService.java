package id.remiyu.backend.service;

import id.remiyu.backend.common.exception.ApiException;
import id.remiyu.backend.dto.response.AgendaView;
import id.remiyu.backend.repository.AgendaRepository;
import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgendaService {
  private final AgendaRepository repository;
  private final WorkspaceService workspaces;

  public AgendaService(AgendaRepository repository, WorkspaceService workspaces) {
    this.repository = repository;
    this.workspaces = workspaces;
  }

  public List<AgendaView> list(UUID user, UUID workspace, int limit, int offset) {
    workspaces.requireMember(workspace, user);
    return repository.list(workspace, limit, offset);
  }

  @Transactional
  public AgendaView edit(UUID user, UUID workspace, UUID id, String title, OffsetDateTime starts) {
    workspaces.requireAdmin(workspace, user);
    repository.get(workspace, id).orElseThrow(ApiException::notFound);
    repository.edit(workspace, id, title.trim(), starts);
    return repository.get(workspace, id).orElseThrow();
  }

  @Transactional
  public AgendaView confirm(UUID user, UUID workspace, UUID id) {
    workspaces.requireAdmin(workspace, user);
    var agenda = repository.get(workspace, id).orElseThrow(ApiException::notFound);
    if (!agenda.status().equals("DRAFT")) throw ApiException.badRequest("Agenda bukan draft.");
    repository.confirm(workspace, id);
    return repository.get(workspace, id).orElseThrow();
  }

  @Transactional
  public void delete(UUID user, UUID workspace, UUID id) {
    workspaces.requireAdmin(workspace, user);
    repository.get(workspace, id).orElseThrow(ApiException::notFound);
    repository.delete(workspace, id);
  }
}
