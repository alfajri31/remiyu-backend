package id.remiyu.backend.service.impl;

import id.remiyu.backend.common.exception.ApiException;
import id.remiyu.backend.dto.ai.AiContracts;
import id.remiyu.backend.dto.response.RoutineView;
import id.remiyu.backend.integration.ai.AiGateway;
import id.remiyu.backend.model.enums.RoutineStatus;
import id.remiyu.backend.repository.ImportRepository;
import id.remiyu.backend.repository.RoutineRepository;
import id.remiyu.backend.service.ChannelService;
import id.remiyu.backend.service.RoutineService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoutineServiceImpl implements RoutineService {
  private final ChannelService channels;
  private final RoutineRepository routines;
  private final ImportRepository imports;
  private final AiGateway ai;

  @Override
  public List<RoutineView> list(UUID user, UUID channel) {
    channels.requireMember(channel, user);
    return routines.list(channel);
  }

  @Override
  @Transactional
  public List<RoutineView> detect(UUID user, UUID channel) {
    channels.requireAdmin(channel, user);
    var messages = imports.recentMessages(channel);
    if (messages.isEmpty()) return routines.list(channel);
    ai.detectRoutines(new AiContracts.RoutineSuggestionRequest(channel, user, messages))
        .routines()
        .forEach(
            draft -> {
              if (!routines.exists(channel, draft.title()))
                routines.insert(
                    channel,
                    draft.title(),
                    draft.schedule(),
                    draft.source(),
                    draft.quote(),
                    draft.sourceMessageId());
            });
    return routines.list(channel);
  }

  @Override
  public RoutineView activate(UUID user, UUID channel, UUID routine) {
    return change(user, channel, routine, RoutineStatus.ACTIVE);
  }

  @Override
  public RoutineView pause(UUID user, UUID channel, UUID routine) {
    return change(user, channel, routine, RoutineStatus.PAUSED);
  }

  @Override
  public RoutineView dismiss(UUID user, UUID channel, UUID routine) {
    return change(user, channel, routine, RoutineStatus.DISMISSED);
  }

  private RoutineView change(UUID user, UUID channel, UUID routine, RoutineStatus status) {
    channels.requireAdmin(channel, user);
    routines.get(channel, routine).orElseThrow(ApiException::notFound);
    return routines.save(channel, routine, status);
  }
}
