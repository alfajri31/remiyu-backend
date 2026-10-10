package id.remiyu.backend.dto.response;

import id.remiyu.backend.model.enums.RoutineStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RoutineView(
    UUID id,
    String title,
    String schedule,
    String source,
    String quote,
    RoutineStatus status,
    OffsetDateTime createdAt) {}
