package id.remiyu.backend.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import lombok.*;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class WorkspaceMemberId implements Serializable {
  private UUID workspaceId;
  private UUID userId;
}
