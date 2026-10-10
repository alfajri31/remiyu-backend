package id.remiyu.backend.integration.ai;

import id.remiyu.backend.dto.ai.AiContracts;

public interface AiGateway {
  AiContracts.ExtractionResponse extract(AiContracts.ExtractionRequest request);

  AiContracts.WorkspaceSummaryResponse summarize(AiContracts.WorkspaceSummaryRequest request);

  AiContracts.RoutineSuggestionResponse detectRoutines(
      AiContracts.RoutineSuggestionRequest request);
}
