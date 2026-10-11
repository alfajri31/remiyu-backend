package id.remiyu.backend.dto.response;

import id.remiyu.backend.model.enums.TaskPriority;
import id.remiyu.backend.model.enums.TaskStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record TaskView(
    UUID id,
    UUID channelId,
    UUID importId,
    String title,
    String description,
    String assigneeName,
    OffsetDateTime dueAt,
    TaskPriority priority,
    TaskStatus status,
    boolean submitted,
    List<UUID> sourceMessageIds,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {}
