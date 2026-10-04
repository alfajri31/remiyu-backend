package id.remiyu.backend.service;

import id.remiyu.backend.common.exception.ApiException;
import id.remiyu.backend.dto.response.InvitationResponse;
import id.remiyu.backend.dto.response.MemberView;
import id.remiyu.backend.dto.response.WorkspaceView;
import id.remiyu.backend.repository.WorkspaceRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkspaceService {
  private final WorkspaceRepository repository;

  public WorkspaceService(WorkspaceRepository repository) {
    this.repository = repository;
  }

  public List<WorkspaceView> list(UUID user) {
    return repository.list(user);
  }

  public void requireMember(UUID workspace, UUID user) {
    if (!repository.member(workspace, user)) throw ApiException.notFound();
  }

  public void requireAdmin(UUID workspace, UUID user) {
    if (!repository.owner(workspace, user)) throw ApiException.notFound();
  }

  @Transactional
  public WorkspaceView create(UUID user, String name, String platform, String account) {
    repository.lockOwner(user);
    String billing = repository.ownedCount(user) == 0 ? "FREE" : "PAYMENT_REQUIRED";
    UUID id = UUID.randomUUID();
    repository.insert(id, user, name.trim(), platform, account, billing);
    return repository.get(id);
  }

  public WorkspaceView get(UUID workspace, UUID user) {
    requireMember(workspace, user);
    return repository.get(workspace);
  }

  public InvitationResponse invite(UUID workspace, UUID user) {
    requireAdmin(workspace, user);
    var token = repository.invite(workspace);
    return new InvitationResponse(workspace, token, "/api/v1/workspaces/join/" + token);
  }

  @Transactional
  public WorkspaceView join(UUID token, UUID user) {
    UUID id = repository.invitedWorkspace(token).orElseThrow(ApiException::notFound);
    if (!repository.member(id, user)) repository.addMember(id, user, "MEMBER");
    return repository.get(id);
  }

  public List<MemberView> members(UUID workspace, UUID user) {
    requireMember(workspace, user);
    return repository.members(workspace);
  }
}
