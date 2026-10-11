package id.remiyu.backend.repository;

import id.remiyu.backend.dto.response.DashboardAgendaView;
import id.remiyu.backend.dto.response.DashboardTaskView;
import id.remiyu.backend.model.enums.AgendaStatus;
import id.remiyu.backend.model.enums.TaskPriority;
import id.remiyu.backend.model.enums.TaskStatus;
import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DashboardRepository {
  private static final String ACTIVE_TASKS = "('TODO','IN_PROGRESS')";
  private static final String CONFIRMED_AGENDAS = "'CONFIRMED'";

  private final JdbcClient jdbc;

  public long countActiveTasks(UUID userId, OffsetDateTime startsAt, OffsetDateTime endsAt) {
    return jdbc.sql(
            "SELECT count(*) FROM tasks t JOIN channels_members m ON m.channel_id=t.channel_id "
                + "WHERE m.user_id=:user AND t.status IN "
                + ACTIVE_TASKS
                + " AND t.due_at>=:start AND t.due_at<:end")
        .param("user", userId)
        .param("start", startsAt)
        .param("end", endsAt)
        .query(Long.class)
        .single();
  }

  public long countConfirmedAgendas(UUID userId, OffsetDateTime startsAt, OffsetDateTime endsAt) {
    return jdbc.sql(
            "SELECT count(*) FROM agenda_entries a JOIN channels_members m ON m.channel_id=a.channel_id "
                + "WHERE m.user_id=:user AND a.status="
                + CONFIRMED_AGENDAS
                + " AND a.starts_at>=:start AND a.starts_at<:end")
        .param("user", userId)
        .param("start", startsAt)
        .param("end", endsAt)
        .query(Long.class)
        .single();
  }

  public Map<TaskPriority, Long> countActiveTasksByPriority(
      UUID userId, OffsetDateTime startsAt, OffsetDateTime endsAt) {
    Map<TaskPriority, Long> counts = new EnumMap<>(TaskPriority.class);
    jdbc.sql(
            "SELECT t.priority, count(*) AS total FROM tasks t JOIN channels_members m ON m.channel_id=t.channel_id "
                + "WHERE m.user_id=:user AND t.status IN "
                + ACTIVE_TASKS
                + " AND t.due_at>=:start AND t.due_at<:end GROUP BY t.priority")
        .param("user", userId)
        .param("start", startsAt)
        .param("end", endsAt)
        .query(
            (rs, rowNum) ->
                Map.entry(TaskPriority.valueOf(rs.getString("priority")), rs.getLong("total")))
        .list()
        .forEach(entry -> counts.put(entry.getKey(), entry.getValue()));
    return counts;
  }

  public Optional<DashboardTaskView> nextTask(UUID userId, OffsetDateTime now) {
    return jdbc.sql(
            "SELECT t.id,t.channel_id,t.title,t.due_at,t.priority,t.status FROM tasks t "
                + "JOIN channels_members m ON m.channel_id=t.channel_id "
                + "WHERE m.user_id=:user AND t.status IN "
                + ACTIVE_TASKS
                + " AND t.due_at>=:now "
                + "ORDER BY t.due_at, CASE t.priority WHEN 'HIGH' THEN 0 WHEN 'MEDIUM' THEN 1 ELSE 2 END, t.created_at LIMIT 1")
        .param("user", userId)
        .param("now", now)
        .query(
            (rs, rowNum) ->
                new DashboardTaskView(
                    rs.getObject("id", UUID.class),
                    rs.getObject("channel_id", UUID.class),
                    rs.getString("title"),
                    rs.getObject("due_at", OffsetDateTime.class),
                    TaskPriority.valueOf(rs.getString("priority")),
                    TaskStatus.valueOf(rs.getString("status"))))
        .optional();
  }

  public Optional<DashboardAgendaView> nextAgenda(UUID userId, OffsetDateTime now) {
    return jdbc.sql(
            "SELECT a.id,a.channel_id,a.title,a.starts_at,a.status FROM agenda_entries a "
                + "JOIN channels_members m ON m.channel_id=a.channel_id "
                + "WHERE m.user_id=:user AND a.status="
                + CONFIRMED_AGENDAS
                + " AND a.starts_at>=:now ORDER BY a.starts_at,a.created_at LIMIT 1")
        .param("user", userId)
        .param("now", now)
        .query(
            (rs, rowNum) ->
                new DashboardAgendaView(
                    rs.getObject("id", UUID.class),
                    rs.getObject("channel_id", UUID.class),
                    rs.getString("title"),
                    rs.getObject("starts_at", OffsetDateTime.class),
                    AgendaStatus.valueOf(rs.getString("status"))))
        .optional();
  }
}
