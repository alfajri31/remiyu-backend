package id.remiyu.backend.repository.jpa;

import id.remiyu.backend.entity.ChatMessageEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageJpaRepository extends JpaRepository<ChatMessageEntity, UUID> {
  boolean existsByChannelIdAndMessageHash(UUID channelId, String messageHash);

  Optional<ChatMessageEntity> findByIdAndChannelId(UUID id, UUID channelId);
}
