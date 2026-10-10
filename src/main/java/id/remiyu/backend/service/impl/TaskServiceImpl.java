package id.remiyu.backend.service.impl;

import id.remiyu.backend.common.exception.ApiException;
import id.remiyu.backend.dto.request.TaskRequest;
import id.remiyu.backend.dto.response.PageResponse;
import id.remiyu.backend.dto.response.TaskView;
import id.remiyu.backend.model.enums.TaskStatus;
import id.remiyu.backend.repository.TaskRepository;
import id.remiyu.backend.service.TaskService;
import id.remiyu.backend.service.WorkspaceService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
  private static final int MAX_PAGE_SIZE = 100;

  private final TaskRepository repository;
  private final WorkspaceService workspaces;

  @Override
  public PageResponse<TaskView> list(
      UUID user, UUID workspace, TaskStatus status, Pageable pageable) {
    workspaces.requireMember(workspace, user);
    if (pageable.getPageSize() > MAX_PAGE_SIZE) {
      throw ApiException.badRequest("Ukuran halaman maksimal " + MAX_PAGE_SIZE + ".");
    }
    var page = repository.list(workspace, status, pageable);
    return new PageResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isFirst(),
        page.isLast());
  }

  @Override
  public TaskView get(UUID user, UUID workspace, UUID id) {
    workspaces.requireMember(workspace, user);
    return repository.get(workspace, id).orElseThrow(ApiException::notFound);
  }

  @Override
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
            TaskStatus.TODO,
            List.of());
    return repository.get(workspace, id).orElseThrow();
  }

  @Override
  @Transactional
  public TaskView edit(UUID user, UUID workspace, UUID id, TaskRequest body) {
    workspaces.requireAdmin(workspace, user);
    var task = get(user, workspace, id);
    return repository.save(
        workspace,
        id,
        body.title().trim(),
        body.description(),
        body.assigneeName(),
        body.dueAt(),
        body.priority(),
        task.status());
  }

  @Override
  @Transactional
  public TaskView status(UUID user, UUID workspace, UUID id, TaskStatus status) {
    var task = get(user, workspace, id);
    if (task.status() == TaskStatus.DRAFT)
      throw ApiException.badRequest("Draft harus direview melalui confirm terlebih dahulu.");
    return repository.save(
        workspace,
        id,
        task.title(),
        task.description(),
        task.assigneeName(),
        task.dueAt(),
        task.priority(),
        status);
  }

  @Override
  @Transactional
  public TaskView confirm(UUID user, UUID workspace, UUID id) {
    workspaces.requireAdmin(workspace, user);
    var task = get(user, workspace, id);
    if (task.status() != TaskStatus.DRAFT) throw ApiException.badRequest("Tugas bukan draft.");
    return repository.save(
        workspace,
        id,
        task.title(),
        task.description(),
        task.assigneeName(),
        task.dueAt(),
        task.priority(),
        TaskStatus.TODO);
  }

  @Override
  @Transactional
  public void delete(UUID user, UUID workspace, UUID id) {
    workspaces.requireAdmin(workspace, user);
    get(user, workspace, id);
    repository.delete(workspace, id);
  }
}
