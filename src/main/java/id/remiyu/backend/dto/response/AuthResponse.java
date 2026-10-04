package id.remiyu.backend.dto.response;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

public record AuthResponse(
    String accessToken, String tokenType, OffsetDateTime expiresAt, UserView user) {}
