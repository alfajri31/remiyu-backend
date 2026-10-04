package id.remiyu.backend.model;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

public record ParsedChatMessage(String sender, OffsetDateTime sentAt, String text) {}
