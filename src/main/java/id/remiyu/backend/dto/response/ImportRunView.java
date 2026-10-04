package id.remiyu.backend.dto.response;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ImportRunView(
    UUID id,
    UUID workspaceId,
    String filename,
    int newMessages,
    int duplicateMessages,
    String aiProvider,
    String status,
    OffsetDateTime createdAt) {}
