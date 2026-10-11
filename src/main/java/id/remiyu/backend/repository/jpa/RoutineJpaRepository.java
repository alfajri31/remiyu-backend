package id.remiyu.backend.repository.jpa;

import id.remiyu.backend.entity.RoutineEntity;
import id.remiyu.backend.model.enums.RoutineStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoutineJpaRepository extends JpaRepository<RoutineEntity, UUID> {
  List<RoutineEntity> findByChannelIdAndStatusNotOrderByCreatedAtDesc(
      UUID channelId, RoutineStatus status);

  boolean existsByChannelIdAndTitle(UUID channelId, String title);

  Optional<RoutineEntity> findByIdAndChannelId(UUID id, UUID channelId);
}
