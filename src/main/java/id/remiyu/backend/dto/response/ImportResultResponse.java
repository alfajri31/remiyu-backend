package id.remiyu.backend.dto.response;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

public record ImportResultResponse(
    UUID importId,
    int newMessages,
    int duplicateMessages,
    List<UUID> taskIds,
    List<UUID> agendaIds,
    String status,
    String aiProvider) {}
