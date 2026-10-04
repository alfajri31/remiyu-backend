package id.remiyu.backend.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record TaskView(
    UUID id,
    UUID workspaceId,
    UUID importId,
    String title,
    String description,
    String assigneeName,
    OffsetDateTime dueAt,
    String priority,
    String status,
    boolean submitted,
    List<UUID> sourceMessageIds,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {}
