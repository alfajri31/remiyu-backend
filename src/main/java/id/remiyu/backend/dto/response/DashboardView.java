package id.remiyu.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardView {
  private DashboardPeriodView period;
  private long taskCount;
  private long agendaCount;
  private DashboardPriorityView prioritiesToday;
  private DashboardTaskView nextTask;
  private DashboardAgendaView nextAgenda;
}
