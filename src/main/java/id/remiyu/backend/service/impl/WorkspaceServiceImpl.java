package id.remiyu.backend.service.impl;

import id.remiyu.backend.common.exception.ApiException;
import id.remiyu.backend.dto.response.InvitationResponse;
import id.remiyu.backend.dto.response.MemberView;
import id.remiyu.backend.dto.response.WorkspaceView;
import id.remiyu.backend.model.enums.BillingStatus;
import id.remiyu.backend.model.enums.Platform;
import id.remiyu.backend.model.enums.WorkspaceRole;
import id.remiyu.backend.repository.WorkspaceRepository;
import id.remiyu.backend.service.WorkspaceService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {
  private final WorkspaceRepository repository;

  @Override
  public List<WorkspaceView> list(UUID user) {
    return repository.list(user);
  }

  @Override
  public void requireMember(UUID workspace, UUID user) {
    if (!repository.member(workspace, user)) throw ApiException.notFound();
  }

  @Override
  public void requireAdmin(UUID workspace, UUID user) {
    if (!repository.owner(workspace, user)) throw ApiException.notFound();
  }

  @Override
  @Transactional
  public WorkspaceView create(UUID user, String name, Platform platform, String account) {
    repository.lockOwner(user);
    BillingStatus billing =
        repository.ownedCount(user) == 0 ? BillingStatus.FREE : BillingStatus.PAYMENT_REQUIRED;
    UUID id = UUID.randomUUID();
    repository.insert(id, user, name.trim(), platform, account, billing);
    return repository.get(id);
  }

  @Override
  public WorkspaceView get(UUID workspace, UUID user) {
    requireMember(workspace, user);
    return repository.get(workspace);
  }

  @Override
  public InvitationResponse invite(UUID workspace, UUID user) {
    requireAdmin(workspace, user);
    var token = repository.invite(workspace);
    return new InvitationResponse(workspace, token, "/api/v1/workspaces/join/" + token);
  }

  @Override
  @Transactional
  public WorkspaceView join(UUID token, UUID user) {
    UUID id = repository.invitedWorkspace(token).orElseThrow(ApiException::notFound);
    if (!repository.member(id, user)) repository.addMember(id, user, WorkspaceRole.MEMBER);
    return repository.get(id);
  }

  @Override
  public List<MemberView> members(UUID workspace, UUID user) {
    requireMember(workspace, user);
    return repository.members(workspace);
  }
}
