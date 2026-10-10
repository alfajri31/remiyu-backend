package id.remiyu.backend.dto.ai;

import id.remiyu.backend.model.enums.TaskPriority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class AiContracts {
  private AiContracts() {}

  public record Message(UUID id, String sender, OffsetDateTime sentAt, String text) {}

  public record ExtractionRequest(
      UUID requestId, UUID workspaceId, UUID userId, List<Message> messages) {}

  public record TaskDraft(
      @NotBlank @Size(max = 200) String title,
      @Size(max = 4000) String description,
      @Size(max = 100) String assigneeName,
      OffsetDateTime dueAt,
      TaskPriority priority,
      @NotEmpty @Size(max = 100) List<@NotNull UUID> sourceMessageIds) {}

  public record AgendaDraft(
      @NotBlank @Size(max = 200) String title,
      OffsetDateTime startsAt,
      @NotEmpty @Size(max = 100) List<@NotNull UUID> sourceMessageIds) {}

  public record ExtractionResponse(
      @NotNull @Size(max = 100) List<@NotNull @Valid TaskDraft> tasks,
      @NotNull @Size(max = 100) List<@NotNull @Valid AgendaDraft> agendas) {}

  public record SummaryItem(String title, String status, String priority, OffsetDateTime at) {}

  public record WorkspaceSummaryRequest(
      UUID workspaceId,
      UUID userId,
      String workspaceName,
      List<SummaryItem> tasks,
      List<SummaryItem> agendas) {}

  public record WorkspaceSummaryResponse(
      @NotBlank @Size(max = 2000) String currentState,
      @NotBlank @Size(max = 2000) String attention,
      @NotBlank @Size(max = 2000) String nextSteps) {}

  public record RoutineSuggestionRequest(UUID workspaceId, UUID userId, List<Message> messages) {}

  public record RoutineDraft(
      @NotBlank @Size(max = 200) String title,
      @NotBlank @Size(max = 200) String schedule,
      @NotBlank @Size(max = 200) String source,
      @NotBlank @Size(max = 1000) String quote,
      @NotNull UUID sourceMessageId) {}

  public record RoutineSuggestionResponse(
      @NotNull @Size(max = 20) List<@NotNull @Valid RoutineDraft> routines) {}
}
