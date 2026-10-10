package id.remiyu.backend.service.impl;

import id.remiyu.backend.dto.response.DashboardAgendaView;
import id.remiyu.backend.dto.response.DashboardPeriodView;
import id.remiyu.backend.dto.response.DashboardPriorityView;
import id.remiyu.backend.dto.response.DashboardTaskView;
import id.remiyu.backend.dto.response.DashboardView;
import id.remiyu.backend.model.enums.TaskPriority;
import id.remiyu.backend.repository.DashboardRepository;
import id.remiyu.backend.service.DashboardService;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
  private static final ZoneId JAKARTA_ZONE = ZoneId.of("Asia/Jakarta");
  private static final int OVERVIEW_DAYS = 14;

  private final DashboardRepository repository;

  @Override
  @Transactional(readOnly = true)
  public DashboardView get(UUID userId) {
    OffsetDateTime now = OffsetDateTime.now(JAKARTA_ZONE);
    OffsetDateTime periodStart =
        LocalDate.now(JAKARTA_ZONE).atStartOfDay(JAKARTA_ZONE).toOffsetDateTime();
    OffsetDateTime periodEnd = periodStart.plusDays(OVERVIEW_DAYS);
    var priorityCounts =
        repository.countActiveTasksByPriority(userId, periodStart, periodStart.plusDays(1));

    DashboardTaskView nextTask = repository.nextTask(userId, now).orElse(null);
    DashboardAgendaView nextAgenda = repository.nextAgenda(userId, now).orElse(null);
    return new DashboardView(
        new DashboardPeriodView(periodStart, periodEnd),
        repository.countActiveTasks(userId, periodStart, periodEnd),
        repository.countConfirmedAgendas(userId, periodStart, periodEnd),
        new DashboardPriorityView(
            priorityCounts.getOrDefault(TaskPriority.HIGH, 0L),
            priorityCounts.getOrDefault(TaskPriority.MEDIUM, 0L),
            priorityCounts.getOrDefault(TaskPriority.LOW, 0L)),
        nextTask,
        nextAgenda);
  }
}
