package id.remiyu.backend.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AgendaView(
    UUID id, String title, OffsetDateTime startsAt, String status, List<UUID> sourceMessageIds) {}
