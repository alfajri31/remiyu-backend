package id.remiyu.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "task_submissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaskSubmissionEntity {
  @Id private UUID id;
  @Column(name = "task_id", nullable = false) private UUID taskId;
  @Column(name = "user_id", nullable = false) private UUID userId;
  private String link;
  private String note;
  private String filename;
  @Lob private byte[] content;
  @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
}
