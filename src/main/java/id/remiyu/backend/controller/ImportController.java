package id.remiyu.backend.controller;

import id.remiyu.backend.dto.response.ImportResultResponse;
import id.remiyu.backend.dto.response.ImportRunView;
import id.remiyu.backend.dto.response.UserView;
import id.remiyu.backend.service.ImportService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/channels/{channel}/imports")
@RequiredArgsConstructor
public class ImportController {
  private final ImportService service;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public ImportResultResponse upload(
      @AuthenticationPrincipal UserView user,
      @PathVariable UUID channel,
      @RequestPart("file") MultipartFile file) {
    return service.upload(user.id(), channel, file);
  }

  @GetMapping
  public List<ImportRunView> list(
      @AuthenticationPrincipal UserView user, @PathVariable UUID channel) {
    return service.list(user.id(), channel);
  }
}
