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

  public Page<TaskView> list(UUID channel, TaskStatus status, Pageable pageable) {
    Pageable sorted =
        PageRequest.of(
            pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdAt").descending());
    return tasks
        .findAll(
            TaskSpecifications.channelId(channel).and(TaskSpecifications.status(status)), sorted)
        .map(this::view);
  }

  public Optional<TaskView> get(UUID channel, UUID id) {
    return tasks.findByIdAndChannelId(id, channel).map(this::view);
  }

  public UUID create(
      UUID channel,
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
                  .channelId(channel)
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
      UUID channel,
      UUID id,
      String title,
      String description,
      String assignee,
      OffsetDateTime due,
      TaskPriority priority,
      TaskStatus status) {
    TaskEntity task = tasks.findByIdAndChannelId(id, channel).orElseThrow();
    task.setTitle(title);
    task.setDescription(description);
    task.setAssigneeName(assignee);
    task.setDueAt(due);
    task.setPriority(priority);
    task.setStatus(status);
    task.setUpdatedAt(OffsetDateTime.now());
    return view(tasks.save(task));
  }

  public void delete(UUID channel, UUID id) {
    tasks.findByIdAndChannelId(id, channel).ifPresent(tasks::delete);
  }

  private TaskView view(TaskEntity task) {
    try {
      return new TaskView(
          task.getId(),
          task.getChannelId(),
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
