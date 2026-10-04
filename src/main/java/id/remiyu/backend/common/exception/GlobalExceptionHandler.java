package id.remiyu.backend.common.exception;

import id.remiyu.backend.dto.response.ApiError;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private ResponseEntity<ApiError> response(
      HttpStatus status, String message, Map<String, String> errors) {
    return ResponseEntity.status(status)
        .body(new ApiError(Instant.now(), status.value(), message, errors));
  }

  @ExceptionHandler(ApiException.class)
  ResponseEntity<ApiError> api(ApiException e) {
    return response(e.status(), e.getMessage(), Map.of());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiError> validation(MethodArgumentNotValidException e) {
    var errors = new LinkedHashMap<String, String>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(f -> errors.put(f.getField(), f.getDefaultMessage()));
    return response(HttpStatus.BAD_REQUEST, "Data tidak valid.", errors);
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class,
    HandlerMethodValidationException.class
  })
  ResponseEntity<ApiError> malformed(Exception e) {
    return response(HttpStatus.BAD_REQUEST, "Format data tidak valid.", Map.of());
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  ResponseEntity<ApiError> large(Exception e) {
    return response(HttpStatus.PAYLOAD_TOO_LARGE, "File maksimal 1 MB.", Map.of());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<ApiError> conflict(Exception e) {
    return response(HttpStatus.CONFLICT, "Data sudah ada atau masih digunakan.", Map.of());
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiError> unexpected(Exception e) {
    log.error("Request failed: {}", e.getClass().getSimpleName());
    return response(HttpStatus.INTERNAL_SERVER_ERROR, "Terjadi kesalahan pada server.", Map.of());
  }
}
