package id.remiyu.backend.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.remiyu.backend.dto.response.TaskView;
import id.remiyu.backend.entity.TaskEntity;
import id.remiyu.backend.model.enums.ReviewStatus;
import id.remiyu.backend.model.enums.TaskPriority;
import id.remiyu.backend.model.enums.TaskStatus;
import id.remiyu.backend.repository.jpa.TaskJpaRepository;
import id.remiyu.backend.repository.specification.TaskSpecifications;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TaskRepository {
  private final TaskJpaRepository tasks;
  private final ObjectMapper mapper;

  public Page<TaskView> list(UUID workspace, TaskStatus status, Pageable pageable) {
    Pageable sorted =
        PageRequest.of(
            pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdAt").descending());
    return tasks
        .findAll(
            TaskSpecifications.workspaceId(workspace).and(TaskSpecifications.status(status)),
            sorted)
        .map(this::view);
  }

  public Optional<TaskView> get(UUID workspace, UUID id) {
    return tasks.findByIdAndWorkspaceId(id, workspace).map(this::view);
  }

  public UUID create(
      UUID workspace,
      UUID importId,
      String title,
      String description,
      String assignee,
      OffsetDateTime due,
      TaskPriority priority,
      TaskStatus status,
      List<UUID> sources) {
    try {
      OffsetDateTime now = OffsetDateTime.now();
      return tasks
          .save(
              TaskEntity.builder()
                  .id(UUID.randomUUID())
                  .workspaceId(workspace)
                  .importId(importId)
                  .title(title)
                  .description(description)
                  .assigneeName(assignee)
                  .dueAt(due)
                  .priority(priority)
                  .status(status)
                  .sourceMessageIds(mapper.writeValueAsString(sources))
                  .chatMessageId(sources.isEmpty() ? null : sources.getFirst())
                  .reviewStatus(ReviewStatus.PENDING)
                  .createdAt(now)
                  .updatedAt(now)
                  .build())
          .getId();
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  public TaskView save(
      UUID workspace,
      UUID id,
      String title,
      String description,
      String assignee,
      OffsetDateTime due,
      TaskPriority priority,
      TaskStatus status) {
    TaskEntity task = tasks.findByIdAndWorkspaceId(id, workspace).orElseThrow();
    task.setTitle(title);
    task.setDescription(description);
    task.setAssigneeName(assignee);
    task.setDueAt(due);
    task.setPriority(priority);
    task.setStatus(status);
    task.setUpdatedAt(OffsetDateTime.now());
    return view(tasks.save(task));
  }

  public void delete(UUID workspace, UUID id) {
    tasks.findByIdAndWorkspaceId(id, workspace).ifPresent(tasks::delete);
  }

  private TaskView view(TaskEntity task) {
    try {
      return new TaskView(
          task.getId(),
          task.getWorkspaceId(),
          task.getImportId(),
          task.getTitle(),
          task.getDescription(),
          task.getAssigneeName(),
          task.getDueAt(),
          task.getPriority(),
          task.getStatus(),
          false,
          mapper.readValue(task.getSourceMessageIds(), new TypeReference<List<UUID>>() {}),
          task.getCreatedAt(),
          task.getUpdatedAt());
    } catch (java.io.IOException e) {
      throw new IllegalStateException("Invalid stored source ids", e);
    }
  }
}
