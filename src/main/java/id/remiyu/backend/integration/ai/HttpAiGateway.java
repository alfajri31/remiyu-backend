package id.remiyu.backend.integration.ai;

import id.remiyu.backend.common.exception.ApiException;
import id.remiyu.backend.dto.ai.AiContracts;
import jakarta.validation.Validator;
import java.time.Duration;
import java.util.HashSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HttpAiGateway implements AiGateway {
  private final RestClient client;
  private final String path;
  private final String summaryPath;
  private final Validator validator;

  public HttpAiGateway(
      RestClient.Builder builder,
      Validator validator,
      @Value("${remiyu.ai.base-url}") String baseUrl,
      @Value("${remiyu.ai.extract-path}") String path,
      @Value("${remiyu.ai.summary-path}") String summaryPath,
      @Value("${remiyu.ai.api-key}") String apiKey,
      @Value("${remiyu.ai.connect-timeout}") Duration connect,
      @Value("${remiyu.ai.read-timeout}") Duration read) {
    var factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(connect);
    factory.setReadTimeout(read);
    var configured = builder.baseUrl(baseUrl).requestFactory(factory);
    if (!apiKey.isBlank()) configured.defaultHeader("Authorization", "Bearer " + apiKey);
    this.client = configured.build();
    this.path = path;
    this.summaryPath = summaryPath;
    this.validator = validator;
  }

  @Override
  public AiContracts.ChannelSummaryResponse summarize(AiContracts.ChannelSummaryRequest request) {
    var result = post(summaryPath, request, AiContracts.ChannelSummaryResponse.class);
    if (result == null || !validator.validate(result).isEmpty()) throw invalid();
    return result;
  }

  private <T> T post(String targetPath, Object body, Class<T> responseType) {
    try {
      return client
          .post()
          .uri(targetPath)
          .contentType(MediaType.APPLICATION_JSON)
          .body(body)
          .retrieve()
          .body(responseType);
    } catch (RestClientException e) {
      throw new ApiException(
          HttpStatus.BAD_GATEWAY, "Layanan AI tidak tersedia atau respons tidak valid.");
    }
  }

  @Override
  public AiContracts.ExtractionResponse extract(AiContracts.ExtractionRequest request) {
    AiContracts.ExtractionResponse result;
    try {
      result =
          client
              .post()
              .uri(path)
              .contentType(MediaType.APPLICATION_JSON)
              .body(request)
              .retrieve()
              .body(AiContracts.ExtractionResponse.class);
    } catch (RestClientException e) {
      throw new ApiException(
          HttpStatus.BAD_GATEWAY, "Layanan AI tidak tersedia atau respons tidak valid.");
    }
    if (result == null || !validator.validate(result).isEmpty()) throw invalid();
    validateSourceMessageIds(
        request.messages(),
        java.util.stream.Stream.concat(
                java.util.stream.Stream.concat(
                    result.tasks().stream().flatMap(task -> task.sourceMessageIds().stream()),
                    result.agendas().stream()
                        .flatMap(agenda -> agenda.sourceMessageIds().stream())),
                result.routines().stream().map(AiContracts.RoutineDraft::sourceMessageId))
            .toList());
    return result;
  }

  private void validateSourceMessageIds(
      java.util.List<AiContracts.Message> messages,
      java.util.List<java.util.UUID> sourceMessageIds) {
    var ids = new HashSet<java.util.UUID>();
    messages.forEach(message -> ids.add(message.id()));
    if (!ids.containsAll(sourceMessageIds)) throw invalid();
  }

  private ApiException invalid() {
    return new ApiException(
        HttpStatus.BAD_GATEWAY, "Respons AI tidak sesuai kontrak atau sumber pesan.");
  }
}
