package id.remiyu.backend.entity;

import id.remiyu.backend.model.enums.ImportStatus;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "import_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportRunEntity {
  @Id private UUID id;

  @Column(name = "channel_id", nullable = false)
  private UUID channelId;

  @Column(nullable = false)
  private String filename;

  @Column(name = "new_messages", nullable = false)
  private int newMessages;

  @Column(name = "duplicate_messages", nullable = false)
  private int duplicateMessages;

  @Column(name = "ai_provider", nullable = false)
  private String aiProvider;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ImportStatus status;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;
}
