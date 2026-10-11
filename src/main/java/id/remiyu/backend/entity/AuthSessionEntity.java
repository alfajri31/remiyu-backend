package id.remiyu.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "auth_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthSessionEntity {
  @Id
  @Column(name = "token_hash")
  private String tokenHash;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "expires_at", nullable = false)
  private OffsetDateTime expiresAt;
}
