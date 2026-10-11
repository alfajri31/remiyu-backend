package id.remiyu.backend.dto.response;

import id.remiyu.backend.model.enums.ImportStatus;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ImportRunView(
    UUID id,
    UUID channelId,
    String filename,
    int newMessages,
    int duplicateMessages,
    String aiProvider,
    ImportStatus status,
    OffsetDateTime createdAt) {}
