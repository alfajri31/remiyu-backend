package id.remiyu.backend.controller;

import id.remiyu.backend.dto.request.CreateWorkspaceRequest;
import id.remiyu.backend.dto.response.InvitationResponse;
import id.remiyu.backend.dto.response.MemberView;
import id.remiyu.backend.dto.response.UserView;
import id.remiyu.backend.dto.response.WorkspaceView;
import id.remiyu.backend.service.WorkspaceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {
  private final WorkspaceService service;

  @GetMapping
  public List<WorkspaceView> list(@AuthenticationPrincipal UserView user) {
    return service.list(user.id());
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public WorkspaceView create(
      @AuthenticationPrincipal UserView user, @Valid @RequestBody CreateWorkspaceRequest body) {
    return service.create(user.id(), body.name(), body.platform(), body.account());
  }

  @GetMapping("/{id}")
  public WorkspaceView get(@AuthenticationPrincipal UserView user, @PathVariable UUID id) {
    return service.get(id, user.id());
  }

  @GetMapping("/{id}/members")
  public List<MemberView> members(@AuthenticationPrincipal UserView user, @PathVariable UUID id) {
    return service.members(id, user.id());
  }

  @PostMapping("/{id}/invitation")
  public InvitationResponse invite(@AuthenticationPrincipal UserView user, @PathVariable UUID id) {
    return service.invite(id, user.id());
  }

  @PostMapping("/join/{token}")
  public WorkspaceView join(@AuthenticationPrincipal UserView user, @PathVariable UUID token) {
    return service.join(token, user.id());
  }
}
