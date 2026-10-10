package id.remiyu.backend.controller;

import id.remiyu.backend.dto.response.SourceMessageView;
import id.remiyu.backend.dto.response.UserView;
import id.remiyu.backend.service.MessageService;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workspaces/{workspace}/messages")
@RequiredArgsConstructor
public class MessageController {
  private final MessageService service;

  @GetMapping
  public List<SourceMessageView> sources(
      @AuthenticationPrincipal UserView user,
      @PathVariable UUID workspace,
      @RequestParam @Size(min = 1, max = 100) List<UUID> ids) {
    return service.sources(user.id(), workspace, ids);
  }
}
