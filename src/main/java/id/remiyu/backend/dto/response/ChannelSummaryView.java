package id.remiyu.backend.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChannelSummaryView(
    UUID channelId,
    String currentState,
    String attention,
    String nextSteps,
    String provider,
    OffsetDateTime generatedAt) {}
