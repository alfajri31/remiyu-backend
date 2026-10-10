package id.remiyu.backend.service.impl;

import id.remiyu.backend.common.exception.ApiException;
import id.remiyu.backend.dto.response.AgendaView;
import id.remiyu.backend.dto.response.PageResponse;
import id.remiyu.backend.model.enums.AgendaStatus;
import id.remiyu.backend.repository.AgendaRepository;
import id.remiyu.backend.service.AgendaService;
import id.remiyu.backend.service.WorkspaceService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AgendaServiceImpl implements AgendaService {
  private static final int MAX_PAGE_SIZE = 100;

  private final AgendaRepository repository;
  private final WorkspaceService workspaces;

  @Override
  public PageResponse<AgendaView> list(UUID user, UUID workspace, Pageable pageable) {
    workspaces.requireMember(workspace, user);
    if (pageable.getPageSize() > MAX_PAGE_SIZE) {
      throw ApiException.badRequest("Ukuran halaman maksimal " + MAX_PAGE_SIZE + ".");
    }
    List<AgendaView> content =
        repository.list(workspace, pageable.getPageSize(), pageable.getOffset());
    long total = repository.count(workspace);
    int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / pageable.getPageSize());
    return new PageResponse<>(
        content,
        pageable.getPageNumber(),
        pageable.getPageSize(),
        total,
        totalPages,
        pageable.getPageNumber() == 0,
        totalPages == 0 || pageable.getPageNumber() + 1 >= totalPages);
  }

  @Override
  @Transactional
  public AgendaView edit(UUID user, UUID workspace, UUID id, String title, OffsetDateTime starts) {
    workspaces.requireAdmin(workspace, user);
    repository.get(workspace, id).orElseThrow(ApiException::notFound);
    repository.edit(workspace, id, title.trim(), starts);
    return repository.get(workspace, id).orElseThrow();
  }

  @Override
  @Transactional
  public AgendaView confirm(UUID user, UUID workspace, UUID id) {
    workspaces.requireAdmin(workspace, user);
    var agenda = repository.get(workspace, id).orElseThrow(ApiException::notFound);
    if (agenda.status() != AgendaStatus.DRAFT) throw ApiException.badRequest("Agenda bukan draft.");
    repository.confirm(workspace, id);
    return repository.get(workspace, id).orElseThrow();
  }

  @Override
  @Transactional
  public void delete(UUID user, UUID workspace, UUID id) {
    workspaces.requireAdmin(workspace, user);
    repository.get(workspace, id).orElseThrow(ApiException::notFound);
    repository.delete(workspace, id);
  }
}
