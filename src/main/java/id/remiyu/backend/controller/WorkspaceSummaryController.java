package id.remiyu.backend.controller;

import id.remiyu.backend.dto.response.UserView;
import id.remiyu.backend.dto.response.WorkspaceSummaryView;
import id.remiyu.backend.service.WorkspaceSummaryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/workspaces/{workspace}/summary")
public class WorkspaceSummaryController {
  private final WorkspaceSummaryService service;

  @PostMapping
  public WorkspaceSummaryView generate(
      @AuthenticationPrincipal UserView user, @PathVariable UUID workspace) {
    return service.generate(user.id(), workspace);
  }
}
