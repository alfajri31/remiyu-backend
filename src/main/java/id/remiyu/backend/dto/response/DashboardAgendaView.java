package id.remiyu.backend.dto.response;

import id.remiyu.backend.model.enums.AgendaStatus;
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
public class DashboardAgendaView {
  private UUID id;
  private UUID channelId;
  private String title;
  private OffsetDateTime startsAt;
  private AgendaStatus status;
}
