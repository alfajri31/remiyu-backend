package id.remiyu.backend.service;

import id.remiyu.backend.dto.response.RoutineView;
import java.util.List;
import java.util.UUID;

public interface RoutineService {
  List<RoutineView> list(UUID user, UUID workspace);

  List<RoutineView> detect(UUID user, UUID workspace);

  RoutineView activate(UUID user, UUID workspace, UUID routine);

  RoutineView pause(UUID user, UUID workspace, UUID routine);

  RoutineView dismiss(UUID user, UUID workspace, UUID routine);
}
