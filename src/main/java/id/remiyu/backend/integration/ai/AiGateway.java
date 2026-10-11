package id.remiyu.backend.integration.ai;

import id.remiyu.backend.dto.ai.AiContracts;

public interface AiGateway {
  boolean supportsSplitExtraction();

  AiContracts.ExtractionResponse extract(AiContracts.ExtractionRequest request);

  AiContracts.TaskExtractionResponse extractTasks(AiContracts.TaskExtractionRequest request);

  AiContracts.AgendaExtractionResponse extractAgendas(AiContracts.AgendaExtractionRequest request);

  AiContracts.ChannelSummaryResponse summarize(AiContracts.ChannelSummaryRequest request);

  AiContracts.RoutineSuggestionResponse detectRoutines(
      AiContracts.RoutineSuggestionRequest request);
}
