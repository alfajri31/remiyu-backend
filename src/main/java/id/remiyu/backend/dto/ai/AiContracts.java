package id.remiyu.backend.dto.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class AiContracts {
  private AiContracts() {}

  public record Message(UUID id, String sender, OffsetDateTime sentAt, String text) {}

  public record ExtractionRequest(UUID requestId, UUID workspaceId, List<Message> messages) {}

  public record TaskDraft(
      @NotBlank @Size(max = 200) String title,
      @Size(max = 4000) String description,
      @Size(max = 100) String assigneeName,
      OffsetDateTime dueAt,
      @NotBlank @Pattern(regexp = "LOW|MEDIUM|HIGH") String priority,
      @NotEmpty @Size(max = 100) List<@NotNull UUID> sourceMessageIds) {}

  public record AgendaDraft(
      @NotBlank @Size(max = 200) String title,
      OffsetDateTime startsAt,
      @NotEmpty @Size(max = 100) List<@NotNull UUID> sourceMessageIds) {}

  public record ExtractionResponse(
      @NotNull @Size(max = 100) List<@NotNull @Valid TaskDraft> tasks,
      @NotNull @Size(max = 100) List<@NotNull @Valid AgendaDraft> agendas) {}
}
