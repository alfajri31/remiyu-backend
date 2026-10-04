package id.remiyu.backend.controller;

import id.remiyu.backend.dto.request.AgendaRequest;
import id.remiyu.backend.dto.response.AgendaView;
import id.remiyu.backend.dto.response.UserView;
import id.remiyu.backend.service.AgendaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workspaces/{workspace}/agendas")
public class AgendaController {
  private final AgendaService service;

  public AgendaController(AgendaService service) {
    this.service = service;
  }

  @GetMapping
  public List<AgendaView> list(
      @AuthenticationPrincipal UserView user,
      @PathVariable UUID workspace,
      @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit,
      @RequestParam(defaultValue = "0") @Min(0) int offset) {
    return service.list(user.id(), workspace, limit, offset);
  }

  @PatchMapping("/{id}")
  public AgendaView edit(
      @AuthenticationPrincipal UserView user,
      @PathVariable UUID workspace,
      @PathVariable UUID id,
      @Valid @RequestBody AgendaRequest body) {
    return service.edit(user.id(), workspace, id, body.title(), body.startsAt());
  }

  @PostMapping("/{id}/confirm")
  public AgendaView confirm(
      @AuthenticationPrincipal UserView user, @PathVariable UUID workspace, @PathVariable UUID id) {
    return service.confirm(user.id(), workspace, id);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @AuthenticationPrincipal UserView user, @PathVariable UUID workspace, @PathVariable UUID id) {
    service.delete(user.id(), workspace, id);
  }
}
