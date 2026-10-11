package id.remiyu.backend.service.impl;

import id.remiyu.backend.common.exception.ApiException;
import id.remiyu.backend.dto.request.TaskRequest;
import id.remiyu.backend.dto.response.PageResponse;
import id.remiyu.backend.dto.response.TaskView;
import id.remiyu.backend.model.enums.TaskStatus;
import id.remiyu.backend.repository.TaskRepository;
import id.remiyu.backend.service.ChannelService;
import id.remiyu.backend.service.TaskService;
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
  private final ChannelService channels;

  @Override
  public PageResponse<TaskView> list(
      UUID user, UUID channel, TaskStatus status, Pageable pageable) {
    channels.requireMember(channel, user);
    if (pageable.getPageSize() > MAX_PAGE_SIZE) {
      throw ApiException.badRequest("Ukuran halaman maksimal " + MAX_PAGE_SIZE + ".");
    }
    var page = repository.list(channel, status, pageable);
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
  public TaskView get(UUID user, UUID channel, UUID id) {
    channels.requireMember(channel, user);
    return repository.get(channel, id).orElseThrow(ApiException::notFound);
  }

  @Override
  @Transactional
  public TaskView create(UUID user, UUID channel, TaskRequest body) {
    channels.requireAdmin(channel, user);
    var id =
        repository.create(
            channel,
            null,
            body.title().trim(),
            body.description(),
            body.assigneeName(),
            body.dueAt(),
            body.priority(),
            TaskStatus.TODO,
            List.of());
    return repository.get(channel, id).orElseThrow();
  }

  @Override
  @Transactional
  public TaskView edit(UUID user, UUID channel, UUID id, TaskRequest body) {
    channels.requireAdmin(channel, user);
    var task = get(user, channel, id);
    return repository.save(
        channel,
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
  public TaskView status(UUID user, UUID channel, UUID id, TaskStatus status) {
    var task = get(user, channel, id);
    if (task.status() == TaskStatus.DRAFT)
      throw ApiException.badRequest("Draft harus direview melalui confirm terlebih dahulu.");
    return repository.save(
        channel,
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
  public TaskView confirm(UUID user, UUID channel, UUID id) {
    channels.requireAdmin(channel, user);
    var task = get(user, channel, id);
    if (task.status() != TaskStatus.DRAFT) throw ApiException.badRequest("Tugas bukan draft.");
    return repository.save(
        channel,
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
  public void delete(UUID user, UUID channel, UUID id) {
    channels.requireAdmin(channel, user);
    get(user, channel, id);
    repository.delete(channel, id);
  }
}
