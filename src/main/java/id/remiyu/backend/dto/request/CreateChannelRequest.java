package id.remiyu.backend.dto.request;

import id.remiyu.backend.model.enums.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateChannelRequest(
    @NotBlank @Size(max = 100) String name,
    @NotNull Platform platform,
    @Size(max = 254) String account) {}
