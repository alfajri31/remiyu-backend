package id.remiyu.backend.dto.request;

import id.remiyu.backend.model.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record TaskRequest(
    @NotBlank @Size(max = 200) String title,
    @Size(max = 4000) String description,
    @Size(max = 100) String assigneeName,
    OffsetDateTime dueAt,
    @NotNull TaskPriority priority) {}
