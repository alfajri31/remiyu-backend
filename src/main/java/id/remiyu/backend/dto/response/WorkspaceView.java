package id.remiyu.backend.dto.response;

import id.remiyu.backend.model.enums.BillingStatus;
import id.remiyu.backend.model.enums.ConnectionStatus;
import id.remiyu.backend.model.enums.Platform;
import java.time.OffsetDateTime;
import java.util.UUID;

public record WorkspaceView(
    UUID id,
    String name,
    Platform platform,
    String account,
    ConnectionStatus connectionStatus,
    BillingStatus billingStatus,
    long memberCount,
    OffsetDateTime createdAt) {}
