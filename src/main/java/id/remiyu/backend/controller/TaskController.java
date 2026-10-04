package id.remiyu.backend.controller;

import id.remiyu.backend.dto.request.TaskRequest;
import id.remiyu.backend.dto.request.TaskStatusRequest;
import id.remiyu.backend.dto.response.TaskView;
import id.remiyu.backend.dto.response.UserView;
import id.remiyu.backend.service.TaskService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workspaces/{workspace}/tasks")
public class TaskController {
  private final TaskService service;

  public TaskController(TaskService service) {
    this.service = service;
  }

  @GetMapping
  public List<TaskView> list(
      @AuthenticationPrincipal UserView user,
      @PathVariable UUID workspace,
      @RequestParam(required = false) @Pattern(regexp = "DRAFT|TODO|IN_PROGRESS|DONE")
          String status,
      @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit,
      @RequestParam(defaultValue = "0") @Min(0) int offset) {
    return service.list(user.id(), workspace, status, limit, offset);
  }

  @GetMapping("/{id}")
  public TaskView get(
      @AuthenticationPrincipal UserView user, @PathVariable UUID workspace, @PathVariable UUID id) {
    return service.get(user.id(), workspace, id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TaskView create(
      @AuthenticationPrincipal UserView user,
      @PathVariable UUID workspace,
      @Valid @RequestBody TaskRequest body) {
    return service.create(user.id(), workspace, body);
  }

  @PatchMapping("/{id}")
  public TaskView edit(
      @AuthenticationPrincipal UserView user,
      @PathVariable UUID workspace,
      @PathVariable UUID id,
      @Valid @RequestBody TaskRequest body) {
    return service.edit(user.id(), workspace, id, body);
  }

  @PatchMapping("/{id}/status")
  public TaskView status(
      @AuthenticationPrincipal UserView user,
      @PathVariable UUID workspace,
      @PathVariable UUID id,
      @Valid @RequestBody TaskStatusRequest body) {
    return service.status(user.id(), workspace, id, body.status());
  }

  @PostMapping("/{id}/confirm")
  public TaskView confirm(
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
