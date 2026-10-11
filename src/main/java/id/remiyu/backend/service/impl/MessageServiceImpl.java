package id.remiyu.backend.service.impl;

import id.remiyu.backend.dto.response.SourceMessageView;
import id.remiyu.backend.repository.MessageRepository;
import id.remiyu.backend.service.ChannelService;
import id.remiyu.backend.service.MessageService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
  private final MessageRepository repository;
  private final ChannelService channels;

  @Override
  public List<SourceMessageView> sources(UUID user, UUID channel, List<UUID> ids) {
    channels.requireMember(channel, user);
    return repository.sources(channel, ids);
  }
}
