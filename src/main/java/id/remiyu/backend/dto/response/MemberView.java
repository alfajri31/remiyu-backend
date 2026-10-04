package id.remiyu.backend.dto.response;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record MemberView(UUID id, String name, String email, String role) {}
