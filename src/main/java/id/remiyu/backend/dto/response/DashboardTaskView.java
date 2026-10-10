package id.remiyu.backend.dto.response;

import id.remiyu.backend.model.enums.TaskPriority;
import id.remiyu.backend.model.enums.TaskStatus;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTaskView {
  private UUID id;
  private UUID workspaceId;
  private String title;
  private OffsetDateTime dueAt;
  private TaskPriority priority;
  private TaskStatus status;
}
