package id.remiyu.backend.entity;

import id.remiyu.backend.model.enums.AgendaStatus;
import id.remiyu.backend.model.enums.ReviewStatus;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "agenda_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgendaEntity {
  @Id private UUID id;

  @Column(name = "workspace_id", nullable = false)
  private UUID workspaceId;

  @Column(name = "import_id", nullable = false)
  private UUID importId;

  @Column(nullable = false)
  private String title;

  @Column(name = "starts_at")
  private OffsetDateTime startsAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AgendaStatus status;

  @Column(name = "source_message_ids", nullable = false)
  private String sourceMessageIds;

  @Column(name = "chat_message_id")
  private UUID chatMessageId;

  @Enumerated(EnumType.STRING)
  @Column(name = "review_status", nullable = false)
  private ReviewStatus reviewStatus;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;
}
