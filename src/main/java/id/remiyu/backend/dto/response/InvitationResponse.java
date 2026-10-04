package id.remiyu.backend.dto.response;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record InvitationResponse(UUID workspaceId, UUID inviteToken, String joinPath) {}
