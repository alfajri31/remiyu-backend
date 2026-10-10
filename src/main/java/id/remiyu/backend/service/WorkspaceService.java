package id.remiyu.backend.service;

import id.remiyu.backend.dto.response.InvitationResponse;
import id.remiyu.backend.dto.response.MemberView;
import id.remiyu.backend.dto.response.WorkspaceView;
import id.remiyu.backend.model.enums.Platform;
import java.util.List;
import java.util.UUID;

public interface WorkspaceService {
  List<WorkspaceView> list(UUID user);

  void requireMember(UUID workspace, UUID user);

  void requireAdmin(UUID workspace, UUID user);

  WorkspaceView create(UUID user, String name, Platform platform, String account);

  WorkspaceView get(UUID workspace, UUID user);

  InvitationResponse invite(UUID workspace, UUID user);

  WorkspaceView join(UUID token, UUID user);

  List<MemberView> members(UUID workspace, UUID user);
}
