package id.remiyu.backend.dto.response;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SourceMessageView(UUID id, String sender, OffsetDateTime sentAt, String text) {}
