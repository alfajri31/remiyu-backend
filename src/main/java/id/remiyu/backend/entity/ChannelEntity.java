package id.remiyu.backend.entity;

import id.remiyu.backend.model.enums.*;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "channels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelEntity {
  @Id private UUID id;

  @Column(name = "workspace_id", nullable = false)
  private UUID workspaceId;

  @Column(name = "owner_id", nullable = false)
  private UUID ownerId;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Platform platform;

  private String account;

  @Column(name = "external_group_id")
  private String externalGroupId;

  @Enumerated(EnumType.STRING)
  @Column(name = "connection_status", nullable = false)
  private ConnectionStatus connectionStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "billing_status", nullable = false)
  private BillingStatus billingStatus;

  @Column(name = "invite_token", nullable = false, unique = true)
  private UUID inviteToken;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;
}
