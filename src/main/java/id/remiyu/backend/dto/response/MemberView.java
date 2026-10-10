package id.remiyu.backend.dto.response;

import id.remiyu.backend.model.enums.WorkspaceRole;
import jakarta.validation.constraints.*;
import java.util.UUID;

public record MemberView(UUID id, String name, String email, WorkspaceRole role) {}
