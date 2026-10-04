package id.remiyu.backend.service;

import id.remiyu.backend.common.exception.ApiException;
import id.remiyu.backend.dto.request.TaskRequest;
import id.remiyu.backend.dto.response.TaskView;
import id.remiyu.backend.repository.TaskRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {
  private final TaskRepository repository;
  private final WorkspaceService workspaces;

  public TaskService(TaskRepository repository, WorkspaceService workspaces) {
    this.repository = repository;
    this.workspaces = workspaces;
  }

  public List<TaskView> list(UUID user, UUID workspace, String status, int limit, int offset) {
    workspaces.requireMember(workspace, user);
    return repository.list(workspace, status, limit, offset);
  }

  public TaskView get(UUID user, UUID workspace, UUID id) {
    workspaces.requireMember(workspace, user);
    return repository.get(workspace, id).orElseThrow(ApiException::notFound);
  }

  @Transactional
  public TaskView create(UUID user, UUID workspace, TaskRequest body) {
    workspaces.requireAdmin(workspace, user);
    var id =
        repository.create(
            workspace,
            null,
            body.title().trim(),
            body.description(),
            body.assigneeName(),
            body.dueAt(),
            body.priority(),
            "TODO",
            List.of());
    return repository.get(workspace, id).orElseThrow();
  }

  @Transactional
  public TaskView edit(UUID user, UUID workspace, UUID id, TaskRequest body) {
    workspaces.requireAdmin(workspace, user);
    var task = get(user, workspace, id);
    repository.update(
        workspace,
        id,
        body.title().trim(),
        body.description(),
        body.assigneeName(),
        body.dueAt(),
        body.priority(),
        task.status());
    return repository.get(workspace, id).orElseThrow();
  }

  @Transactional
  public TaskView status(UUID user, UUID workspace, UUID id, String status) {
    var task = get(user, workspace, id);
    if (task.status().equals("DRAFT"))
      throw ApiException.badRequest("Draft harus direview melalui confirm terlebih dahulu.");
    repository.update(
        workspace,
        id,
        task.title(),
        task.description(),
        task.assigneeName(),
        task.dueAt(),
        task.priority(),
        status);
    return repository.get(workspace, id).orElseThrow();
  }

  @Transactional
  public TaskView confirm(UUID user, UUID workspace, UUID id) {
    workspaces.requireAdmin(workspace, user);
    var task = get(user, workspace, id);
    if (!task.status().equals("DRAFT")) throw ApiException.badRequest("Tugas bukan draft.");
    repository.update(
        workspace,
        id,
        task.title(),
        task.description(),
        task.assigneeName(),
        task.dueAt(),
        task.priority(),
        "TODO");
    return repository.get(workspace, id).orElseThrow();
  }

  @Transactional
  public void delete(UUID user, UUID workspace, UUID id) {
    workspaces.requireAdmin(workspace, user);
    get(user, workspace, id);
    repository.delete(workspace, id);
  }
}
