package id.remiyu.backend.dto.request;

import jakarta.validation.constraints.*;

public record LoginRequest(
    @NotBlank @Email String email, @NotBlank @Size(max = 72) String password) {}
