package id.remiyu.backend.service;

import id.remiyu.backend.dto.response.RoutineView;
import java.util.List;
import java.util.UUID;

public interface RoutineService {
  List<RoutineView> list(UUID user, UUID channel);

  List<RoutineView> detect(UUID user, UUID channel);

  RoutineView activate(UUID user, UUID channel, UUID routine);

  RoutineView pause(UUID user, UUID channel, UUID routine);

  RoutineView dismiss(UUID user, UUID channel, UUID routine);
}
