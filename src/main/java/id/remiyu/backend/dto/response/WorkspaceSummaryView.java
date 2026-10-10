package id.remiyu.backend.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WorkspaceSummaryView(
    UUID workspaceId,
    String currentState,
    String attention,
    String nextSteps,
    String provider,
    OffsetDateTime generatedAt) {}
