package id.remiyu.backend.entity;

import id.remiyu.backend.model.enums.WorkspaceRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workspace_members")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkspaceMemberEntity {
  @EmbeddedId private WorkspaceMemberId id;
  @Enumerated(EnumType.STRING) @Column(nullable = false) private WorkspaceRole role;
}
