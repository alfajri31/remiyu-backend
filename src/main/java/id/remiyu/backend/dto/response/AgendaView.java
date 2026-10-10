package id.remiyu.backend.dto.response;

import id.remiyu.backend.model.enums.AgendaStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AgendaView(
    UUID id,
    String title,
    OffsetDateTime startsAt,
    AgendaStatus status,
    List<UUID> sourceMessageIds) {}
