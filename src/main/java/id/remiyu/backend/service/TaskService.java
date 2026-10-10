package id.remiyu.backend.service;

import id.remiyu.backend.dto.request.TaskRequest;
import id.remiyu.backend.dto.response.PageResponse;
import id.remiyu.backend.dto.response.TaskView;
import id.remiyu.backend.model.enums.TaskStatus;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface TaskService {
  PageResponse<TaskView> list(UUID user, UUID workspace, TaskStatus status, Pageable pageable);

  TaskView get(UUID user, UUID workspace, UUID id);

  TaskView create(UUID user, UUID workspace, TaskRequest body);

  TaskView edit(UUID user, UUID workspace, UUID id, TaskRequest body);

  TaskView status(UUID user, UUID workspace, UUID id, TaskStatus status);

  TaskView confirm(UUID user, UUID workspace, UUID id);

  void delete(UUID user, UUID workspace, UUID id);
}
