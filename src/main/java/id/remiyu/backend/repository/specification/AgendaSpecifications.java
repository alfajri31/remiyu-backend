package id.remiyu.backend.repository.specification;

import id.remiyu.backend.entity.AgendaEntity;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class AgendaSpecifications {
  private AgendaSpecifications() {}

  public static Specification<AgendaEntity> workspaceId(UUID workspaceId) {
    return (root, query, builder) -> builder.equal(root.get("workspaceId"), workspaceId);
  }
}
