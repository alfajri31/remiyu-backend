package id.remiyu.backend.repository.jpa;

import id.remiyu.backend.entity.TaskEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TaskJpaRepository
    extends JpaRepository<TaskEntity, UUID>, JpaSpecificationExecutor<TaskEntity> {

  Optional<TaskEntity> findByIdAndWorkspaceId(UUID id, UUID workspaceId);
}
