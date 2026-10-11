package id.remiyu.backend.controller;

import id.remiyu.backend.dto.request.TaskRequest;
import id.remiyu.backend.dto.request.TaskStatusRequest;
import id.remiyu.backend.dto.response.TaskView;
import id.remiyu.backend.dto.response.UserView;
import id.remiyu.backend.model.enums.TaskStatus;
import id.remiyu.backend.service.TaskService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/channels/{channel}/tasks")
@RequiredArgsConstructor
public class TaskController {
  private final TaskService service;

  @GetMapping
  public List<TaskView> list(
      @AuthenticationPrincipal UserView user,
      @PathVariable UUID channel,
      @RequestParam(required = false) TaskStatus status,
      @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit,
      @RequestParam(defaultValue = "0") @Min(0) int offset) {
    if (offset % limit != 0) throw new IllegalArgumentException("offset harus kelipatan limit.");
    return service
        .list(user.id(), channel, status, PageRequest.of(offset / limit, limit))
        .getContent();
  }

  @GetMapping("/{id}")
  public TaskView get(
      @AuthenticationPrincipal UserView user, @PathVariable UUID channel, @PathVariable UUID id) {
    return service.get(user.id(), channel, id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TaskView create(
      @AuthenticationPrincipal UserView user,
      @PathVariable UUID channel,
      @Valid @RequestBody TaskRequest body) {
    return service.create(user.id(), channel, body);
  }

  @PatchMapping("/{id}")
  public TaskView edit(
      @AuthenticationPrincipal UserView user,
      @PathVariable UUID channel,
      @PathVariable UUID id,
      @Valid @RequestBody TaskRequest body) {
    return service.edit(user.id(), channel, id, body);
  }

  @PatchMapping("/{id}/status")
  public TaskView status(
      @AuthenticationPrincipal UserView user,
      @PathVariable UUID channel,
      @PathVariable UUID id,
      @Valid @RequestBody TaskStatusRequest body) {
    return service.status(user.id(), channel, id, body.status());
  }

  @PostMapping("/{id}/confirm")
  public TaskView confirm(
      @AuthenticationPrincipal UserView user, @PathVariable UUID channel, @PathVariable UUID id) {
    return service.confirm(user.id(), channel, id);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @AuthenticationPrincipal UserView user, @PathVariable UUID channel, @PathVariable UUID id) {
    service.delete(user.id(), channel, id);
  }
}
