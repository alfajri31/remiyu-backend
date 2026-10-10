package id.remiyu.backend.repository;

import id.remiyu.backend.dto.response.RoutineView;
import id.remiyu.backend.entity.RoutineEntity;
import id.remiyu.backend.model.enums.ReviewStatus;
import id.remiyu.backend.model.enums.RoutineStatus;
import id.remiyu.backend.repository.jpa.RoutineJpaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoutineRepository {
  private final RoutineJpaRepository routines;

  public List<RoutineView> list(UUID workspace) {
    return routines
        .findByWorkspaceIdAndStatusNotOrderByCreatedAtDesc(workspace, RoutineStatus.DISMISSED)
        .stream()
        .map(this::view)
        .toList();
  }

  public boolean exists(UUID workspace, String title) {
    return routines.existsByWorkspaceIdAndTitle(workspace, title);
  }

  public void insert(
      UUID workspace, String title, String schedule, String source, String quote, UUID messageId) {
    routines.save(
        RoutineEntity.builder()
            .id(UUID.randomUUID())
            .workspaceId(workspace)
            .title(title)
            .schedule(schedule)
            .source(source)
            .quote(quote)
            .chatMessageId(messageId)
            .status(RoutineStatus.SUGGESTED)
            .reviewStatus(ReviewStatus.PENDING)
            .createdAt(OffsetDateTime.now())
            .build());
  }

  public Optional<RoutineView> get(UUID workspace, UUID id) {
    return routines.findByIdAndWorkspaceId(id, workspace).map(this::view);
  }

  public RoutineView save(UUID workspace, UUID id, RoutineStatus status) {
    RoutineEntity routine = routines.findByIdAndWorkspaceId(id, workspace).orElseThrow();
    routine.setStatus(status);
    return view(routines.save(routine));
  }

  private RoutineView view(RoutineEntity routine) {
    return new RoutineView(
        routine.getId(),
        routine.getTitle(),
        routine.getSchedule(),
        routine.getSource(),
        routine.getQuote(),
        routine.getStatus(),
        routine.getCreatedAt());
  }
}
