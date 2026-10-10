package id.remiyu.backend.service.impl;

import id.remiyu.backend.common.exception.ApiException;
import id.remiyu.backend.dto.response.AgendaView;
import id.remiyu.backend.dto.response.PageResponse;
import id.remiyu.backend.model.enums.AgendaStatus;
import id.remiyu.backend.repository.AgendaRepository;
import id.remiyu.backend.service.AgendaService;
import id.remiyu.backend.service.WorkspaceService;
import java.time.OffsetDateTime;
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
    var page = repository.list(workspace, pageable);
    return new PageResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isFirst(),
        page.isLast());
  }

  @Override
  @Transactional
  public AgendaView edit(UUID user, UUID workspace, UUID id, String title, OffsetDateTime starts) {
    workspaces.requireAdmin(workspace, user);
    repository.get(workspace, id).orElseThrow(ApiException::notFound);
    return repository.save(workspace, id, title.trim(), starts);
  }

  @Override
  @Transactional
  public AgendaView confirm(UUID user, UUID workspace, UUID id) {
    workspaces.requireAdmin(workspace, user);
    var agenda = repository.get(workspace, id).orElseThrow(ApiException::notFound);
    if (agenda.status() != AgendaStatus.DRAFT) throw ApiException.badRequest("Agenda bukan draft.");
    return repository.confirm(workspace, id);
  }

  @Override
  @Transactional
  public void delete(UUID user, UUID workspace, UUID id) {
    workspaces.requireAdmin(workspace, user);
    repository.get(workspace, id).orElseThrow(ApiException::notFound);
    repository.delete(workspace, id);
  }
}
