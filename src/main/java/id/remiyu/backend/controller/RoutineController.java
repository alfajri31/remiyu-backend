package id.remiyu.backend.controller;

import id.remiyu.backend.dto.response.RoutineView;
import id.remiyu.backend.dto.response.UserView;
import id.remiyu.backend.service.RoutineService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/channels/{channel}/routines")
public class RoutineController {
  private final RoutineService service;

  @GetMapping
  public List<RoutineView> list(
      @AuthenticationPrincipal UserView user, @PathVariable UUID channel) {
    return service.list(user.id(), channel);
  }

  @PostMapping("/detect")
  public List<RoutineView> detect(
      @AuthenticationPrincipal UserView user, @PathVariable UUID channel) {
    return service.detect(user.id(), channel);
  }

  @PostMapping("/{id}/activate")
  public RoutineView activate(
      @AuthenticationPrincipal UserView user, @PathVariable UUID channel, @PathVariable UUID id) {
    return service.activate(user.id(), channel, id);
  }

  @PostMapping("/{id}/pause")
  public RoutineView pause(
      @AuthenticationPrincipal UserView user, @PathVariable UUID channel, @PathVariable UUID id) {
    return service.pause(user.id(), channel, id);
  }

  @PostMapping("/{id}/dismiss")
  public RoutineView dismiss(
      @AuthenticationPrincipal UserView user, @PathVariable UUID channel, @PathVariable UUID id) {
    return service.dismiss(user.id(), channel, id);
  }
}
