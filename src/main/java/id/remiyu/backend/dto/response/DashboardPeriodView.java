package id.remiyu.backend.dto.response;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardPeriodView {
  private OffsetDateTime startsAt;
  private OffsetDateTime endsAt;
}
