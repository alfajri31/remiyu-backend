package id.remiyu.backend.dto.request;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

public record AgendaRequest(@NotBlank @Size(max = 200) String title, OffsetDateTime startsAt) {}
