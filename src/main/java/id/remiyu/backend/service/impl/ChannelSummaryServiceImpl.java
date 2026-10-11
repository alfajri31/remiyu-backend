package id.remiyu.backend.service.impl;

import id.remiyu.backend.dto.ai.AiContracts;
import id.remiyu.backend.dto.response.ChannelSummaryView;
import id.remiyu.backend.integration.ai.AiGateway;
import id.remiyu.backend.repository.AgendaRepository;
import id.remiyu.backend.repository.TaskRepository;
import id.remiyu.backend.service.ChannelService;
import id.remiyu.backend.service.ChannelSummaryService;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChannelSummaryServiceImpl implements ChannelSummaryService {
  private final ChannelService channels;
  private final TaskRepository tasks;
  private final AgendaRepository agendas;
  private final AiGateway ai;

  @Value("${remiyu.ai.provider}")
  private String provider;

  @Override
  @Transactional(readOnly = true)
  public ChannelSummaryView generate(UUID user, UUID channel) {
    var view = channels.get(channel, user);
    var taskItems =
        tasks.list(channel, null, PageRequest.of(0, 100)).getContent().stream()
            .map(
                t ->
                    new AiContracts.SummaryItem(
                        t.title(), t.status().name(), t.priority().name(), t.dueAt()))
            .toList();
    var agendaItems =
        agendas.list(channel, PageRequest.of(0, 100)).getContent().stream()
            .map(a -> new AiContracts.SummaryItem(a.title(), a.status().name(), null, a.startsAt()))
            .toList();
    var summary =
        ai.summarize(
            new AiContracts.ChannelSummaryRequest(
                channel, user, view.name(), taskItems, agendaItems));
    return new ChannelSummaryView(
        channel,
        summary.currentState(),
        summary.attention(),
        summary.nextSteps(),
        provider,
        OffsetDateTime.now());
  }
}
