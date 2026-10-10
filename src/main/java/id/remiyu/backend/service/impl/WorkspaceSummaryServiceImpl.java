package id.remiyu.backend.service.impl;

import id.remiyu.backend.dto.ai.AiContracts;
import id.remiyu.backend.dto.response.WorkspaceSummaryView;
import id.remiyu.backend.integration.ai.AiGateway;
import id.remiyu.backend.repository.AgendaRepository;
import id.remiyu.backend.repository.TaskRepository;
import id.remiyu.backend.service.WorkspaceService;
import id.remiyu.backend.service.WorkspaceSummaryService;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkspaceSummaryServiceImpl implements WorkspaceSummaryService {
  private final WorkspaceService workspaces;
  private final TaskRepository tasks;
  private final AgendaRepository agendas;
  private final AiGateway ai;

  @Value("${remiyu.ai.provider}")
  private String provider;

  @Override
  @Transactional(readOnly = true)
  public WorkspaceSummaryView generate(UUID user, UUID workspace) {
    var view = workspaces.get(workspace, user);
    var taskItems =
        tasks.list(workspace, null, 100, 0).stream()
            .map(
                t ->
                    new AiContracts.SummaryItem(
                        t.title(), t.status().name(), t.priority().name(), t.dueAt()))
            .toList();
    var agendaItems =
        agendas.list(workspace, 100, 0).stream()
            .map(a -> new AiContracts.SummaryItem(a.title(), a.status().name(), null, a.startsAt()))
            .toList();
    var summary =
        ai.summarize(
            new AiContracts.WorkspaceSummaryRequest(
                workspace, user, view.name(), taskItems, agendaItems));
    return new WorkspaceSummaryView(
        workspace,
        summary.currentState(),
        summary.attention(),
        summary.nextSteps(),
        provider,
        OffsetDateTime.now());
  }
}
