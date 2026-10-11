package id.remiyu.backend.service.impl;

import id.remiyu.backend.common.exception.ApiException;
import id.remiyu.backend.dto.response.ChannelView;
import id.remiyu.backend.dto.response.InvitationResponse;
import id.remiyu.backend.dto.response.MemberView;
import id.remiyu.backend.model.enums.BillingStatus;
import id.remiyu.backend.model.enums.ChannelRole;
import id.remiyu.backend.model.enums.Platform;
import id.remiyu.backend.repository.ChannelRepository;
import id.remiyu.backend.service.ChannelService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChannelServiceImpl implements ChannelService {
  private final ChannelRepository repository;

  @Override
  public List<ChannelView> list(UUID user) {
    return repository.list(user);
  }

  @Override
  public void requireMember(UUID channel, UUID user) {
    if (!repository.member(channel, user)) throw ApiException.notFound();
  }

  @Override
  public void requireAdmin(UUID channel, UUID user) {
    if (!repository.owner(channel, user)) throw ApiException.notFound();
  }

  @Override
  @Transactional
  public ChannelView create(UUID user, String name, Platform platform, String account) {
    repository.lockOwner(user);
    BillingStatus billing =
        repository.ownedCount(user) == 0 ? BillingStatus.FREE : BillingStatus.PAYMENT_REQUIRED;
    UUID id = UUID.randomUUID();
    repository.insert(id, user, name.trim(), platform, account, billing);
    return repository.get(id);
  }

  @Override
  public ChannelView get(UUID channel, UUID user) {
    requireMember(channel, user);
    return repository.get(channel);
  }

  @Override
  public InvitationResponse invite(UUID channel, UUID user) {
    requireAdmin(channel, user);
    var token = repository.invite(channel);
    return new InvitationResponse(channel, token, "/api/v1/channels/join/" + token);
  }

  @Override
  @Transactional
  public ChannelView join(UUID token, UUID user) {
    UUID id = repository.invitedChannel(token).orElseThrow(ApiException::notFound);
    if (!repository.member(id, user)) repository.addMember(id, user, ChannelRole.MEMBER);
    return repository.get(id);
  }

  @Override
  public List<MemberView> members(UUID channel, UUID user) {
    requireMember(channel, user);
    return repository.members(channel);
  }
}
