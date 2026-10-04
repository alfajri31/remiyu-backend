package id.remiyu.backend.dto.request;

import jakarta.validation.constraints.*;

public record CreateWorkspaceRequest(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Pattern(regexp = "WHATSAPP|TELEGRAM|TEAMS") String platform,
    @Size(max = 254) String account) {}
