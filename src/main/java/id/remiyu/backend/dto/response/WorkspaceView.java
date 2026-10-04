package id.remiyu.backend.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WorkspaceView(
    UUID id,
    String name,
    String platform,
    String account,
    String connectionStatus,
    String billingStatus,
    long memberCount,
    OffsetDateTime createdAt) {}
