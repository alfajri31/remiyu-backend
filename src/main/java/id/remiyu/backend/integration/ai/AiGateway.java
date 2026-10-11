package id.remiyu.backend.integration.ai;

import id.remiyu.backend.dto.ai.AiContracts;

public interface AiGateway {
  AiContracts.ExtractionResponse extract(AiContracts.ExtractionRequest request);

  AiContracts.ChannelSummaryResponse summarize(AiContracts.ChannelSummaryRequest request);

  AiContracts.RoutineSuggestionResponse detectRoutines(
      AiContracts.RoutineSuggestionRequest request);
}
