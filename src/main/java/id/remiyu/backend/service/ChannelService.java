package id.remiyu.backend.service;

import id.remiyu.backend.dto.response.ChannelView;
import id.remiyu.backend.dto.response.InvitationResponse;
import id.remiyu.backend.dto.response.MemberView;
import id.remiyu.backend.model.enums.Platform;
import java.util.List;
import java.util.UUID;

public interface ChannelService {
  List<ChannelView> list(UUID user);

  void requireMember(UUID channel, UUID user);

  void requireAdmin(UUID channel, UUID user);

  ChannelView create(UUID user, String name, Platform platform, String account);

  ChannelView get(UUID channel, UUID user);

  InvitationResponse invite(UUID channel, UUID user);

  ChannelView join(UUID token, UUID user);

  List<MemberView> members(UUID channel, UUID user);
}
