package id.remiyu.backend.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SubmissionView(
    UUID id,
    UUID taskId,
    UUID userId,
    String link,
    String note,
    String filename,
    OffsetDateTime createdAt) {}
