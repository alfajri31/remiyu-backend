package id.remiyu.backend.repository.jpa;

import id.remiyu.backend.entity.ChatMessageEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageJpaRepository extends JpaRepository<ChatMessageEntity, UUID> {
  boolean existsByWorkspaceIdAndMessageKey(UUID workspaceId, String messageKey);

  Optional<ChatMessageEntity> findByIdAndWorkspaceId(UUID id, UUID workspaceId);
}
