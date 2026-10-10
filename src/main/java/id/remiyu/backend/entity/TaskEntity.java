package id.remiyu.backend.entity;

import id.remiyu.backend.model.enums.ReviewStatus;
import id.remiyu.backend.model.enums.TaskPriority;
import id.remiyu.backend.model.enums.TaskStatus;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskEntity {
  @Id private UUID id;

  @Column(name = "workspace_id", nullable = false)
  private UUID workspaceId;

  @Column(name = "import_id")
  private UUID importId;

  @Column(nullable = false)
  private String title;

  private String description;

  @Column(name = "assignee_name")
  private String assigneeName;

  @Column(name = "due_at")
  private OffsetDateTime dueAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TaskPriority priority;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TaskStatus status;

  @Column(name = "source_message_ids", nullable = false)
  private String sourceMessageIds;

  @Column(name = "chat_message_id")
  private UUID chatMessageId;

  @Enumerated(EnumType.STRING)
  @Column(name = "review_status", nullable = false)
  private ReviewStatus reviewStatus;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
