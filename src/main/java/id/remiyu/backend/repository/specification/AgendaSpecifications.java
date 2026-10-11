package id.remiyu.backend.repository.specification;

import id.remiyu.backend.entity.AgendaEntity;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class AgendaSpecifications {
  private AgendaSpecifications() {}

  public static Specification<AgendaEntity> channelId(UUID channelId) {
    return (root, query, builder) -> builder.equal(root.get("channelId"), channelId);
  }
}
