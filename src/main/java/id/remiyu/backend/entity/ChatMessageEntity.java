package id.remiyu.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageEntity {
  @Id private UUID id;

  @Column(name = "workspace_id", nullable = false)
  private UUID workspaceId;

  @Column(name = "import_id")
  private UUID importId;

  @Column(name = "message_key", nullable = false)
  private String messageKey;

  @Column(nullable = false)
  private String sender;

  @Column(name = "sent_at", nullable = false)
  private OffsetDateTime sentAt;

  @Column(nullable = false)
  private String content;
}
