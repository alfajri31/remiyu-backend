package id.remiyu.backend.controller;

import id.remiyu.backend.dto.response.ImportResultResponse;
import id.remiyu.backend.dto.response.ImportRunView;
import id.remiyu.backend.dto.response.UserView;
import id.remiyu.backend.service.ImportService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/workspaces/{workspace}/imports")
public class ImportController {
  private final ImportService service;

  public ImportController(ImportService service) {
    this.service = service;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public ImportResultResponse upload(
      @AuthenticationPrincipal UserView user,
      @PathVariable UUID workspace,
      @RequestPart("file") MultipartFile file) {
    return service.upload(user.id(), workspace, file);
  }

  @GetMapping
  public List<ImportRunView> list(
      @AuthenticationPrincipal UserView user, @PathVariable UUID workspace) {
    return service.list(user.id(), workspace);
  }
}
