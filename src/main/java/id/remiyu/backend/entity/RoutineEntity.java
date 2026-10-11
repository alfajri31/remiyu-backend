package id.remiyu.backend.entity;

import id.remiyu.backend.model.enums.ReviewStatus;
import id.remiyu.backend.model.enums.RoutineStatus;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "routines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineEntity {
  @Id private UUID id;

  @Column(name = "channel_id", nullable = false)
  private UUID channelId;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String schedule;

  @Column(nullable = false)
  private String source;

  @Column(nullable = false)
  private String quote;

  @Column(name = "chat_message_id")
  private UUID chatMessageId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RoutineStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "review_status", nullable = false)
  private ReviewStatus reviewStatus;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;
}
