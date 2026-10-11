package id.remiyu.backend.repository.jpa;

import id.remiyu.backend.entity.AgendaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AgendaJpaRepository
    extends JpaRepository<AgendaEntity, UUID>, JpaSpecificationExecutor<AgendaEntity> {

  Optional<AgendaEntity> findByIdAndChannelId(UUID id, UUID channelId);
}
