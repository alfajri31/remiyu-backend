package id.remiyu.backend.dto.response;

import id.remiyu.backend.model.enums.ChannelRole;
import jakarta.validation.constraints.*;
import java.util.UUID;

public record MemberView(UUID id, String name, String email, ChannelRole role) {}
