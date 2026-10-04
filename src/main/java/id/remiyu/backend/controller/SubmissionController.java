package id.remiyu.backend.controller;

import id.remiyu.backend.dto.response.SubmissionView;
import id.remiyu.backend.dto.response.UserView;
import id.remiyu.backend.service.SubmissionService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/workspaces/{workspace}/tasks/{task}/submissions")
public class SubmissionController {
  private final SubmissionService service;

  public SubmissionController(SubmissionService service) {
    this.service = service;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public SubmissionView submit(
      @AuthenticationPrincipal UserView user,
      @PathVariable UUID workspace,
      @PathVariable UUID task,
      @RequestParam(required = false) String link,
      @RequestParam(required = false) String note,
      @RequestPart(required = false) MultipartFile file) {
    return service.submit(user.id(), workspace, task, link, note, file);
  }

  @GetMapping
  public List<SubmissionView> list(
      @AuthenticationPrincipal UserView user,
      @PathVariable UUID workspace,
      @PathVariable UUID task) {
    return service.list(user.id(), workspace, task);
  }
}
