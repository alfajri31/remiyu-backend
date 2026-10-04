package id.remiyu.backend.dto.request;

import jakarta.validation.constraints.*;

public record TaskStatusRequest(
    @NotBlank @Pattern(regexp = "TODO|IN_PROGRESS|DONE") String status) {}
