package id.remiyu.backend.repository.specification;

import id.remiyu.backend.entity.TaskEntity;
import id.remiyu.backend.model.enums.TaskStatus;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class TaskSpecifications {
  private TaskSpecifications() {}

  public static Specification<TaskEntity> workspaceId(UUID workspaceId) {
    return (root, query, builder) -> builder.equal(root.get("workspaceId"), workspaceId);
  }

  public static Specification<TaskEntity> status(TaskStatus status) {
    return status == null
        ? Specification.unrestricted()
        : (root, query, builder) -> builder.equal(root.get("status"), status);
  }
}
