package id.remiyu.backend.dto.request;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

public record TaskRequest(
    @NotBlank @Size(max = 200) String title,
    @Size(max = 4000) String description,
    @Size(max = 100) String assigneeName,
    OffsetDateTime dueAt,
    @NotBlank @Pattern(regexp = "LOW|MEDIUM|HIGH") String priority) {}
