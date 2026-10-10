package id.remiyu.backend.service.impl;

import id.remiyu.backend.common.exception.ApiException;
import id.remiyu.backend.dto.ai.AiContracts;
import id.remiyu.backend.dto.response.RoutineView;
import id.remiyu.backend.integration.ai.AiGateway;
import id.remiyu.backend.model.enums.RoutineStatus;
import id.remiyu.backend.repository.ImportRepository;
import id.remiyu.backend.repository.RoutineRepository;
import id.remiyu.backend.service.RoutineService;
import id.remiyu.backend.service.WorkspaceService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoutineServiceImpl implements RoutineService {
  private final WorkspaceService workspaces;
  private final RoutineRepository routines;
  private final ImportRepository imports;
  private final AiGateway ai;

  @Override
  public List<RoutineView> list(UUID user, UUID workspace) {
    workspaces.requireMember(workspace, user);
    return routines.list(workspace);
  }

  @Override
  @Transactional
  public List<RoutineView> detect(UUID user, UUID workspace) {
    workspaces.requireAdmin(workspace, user);
    var messages = imports.recentMessages(workspace);
    if (messages.isEmpty()) return routines.list(workspace);
    ai.detectRoutines(new AiContracts.RoutineSuggestionRequest(workspace, user, messages))
        .routines()
        .forEach(
            draft -> {
              if (!routines.exists(workspace, draft.title()))
                routines.insert(
                    workspace,
                    draft.title(),
                    draft.schedule(),
                    draft.source(),
                    draft.quote(),
                    draft.sourceMessageId());
            });
    return routines.list(workspace);
  }

  @Override
  public RoutineView activate(UUID user, UUID workspace, UUID routine) {
    return change(user, workspace, routine, RoutineStatus.ACTIVE);
  }

  @Override
  public RoutineView pause(UUID user, UUID workspace, UUID routine) {
    return change(user, workspace, routine, RoutineStatus.PAUSED);
  }

  @Override
  public RoutineView dismiss(UUID user, UUID workspace, UUID routine) {
    return change(user, workspace, routine, RoutineStatus.DISMISSED);
  }

  private RoutineView change(UUID user, UUID workspace, UUID routine, RoutineStatus status) {
    workspaces.requireAdmin(workspace, user);
    routines.get(workspace, routine).orElseThrow(ApiException::notFound);
    routines.status(workspace, routine, status);
    return routines.get(workspace, routine).orElseThrow(ApiException::notFound);
  }
}
