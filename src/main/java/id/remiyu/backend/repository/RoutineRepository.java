package id.remiyu.backend.repository;

import id.remiyu.backend.dto.response.RoutineView;
import id.remiyu.backend.model.enums.RoutineStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoutineRepository {
  private final JdbcClient jdbc;

  private RoutineView map(java.sql.ResultSet rs, int row) throws java.sql.SQLException {
    return new RoutineView(
        rs.getObject("id", UUID.class),
        rs.getString("title"),
        rs.getString("schedule"),
        rs.getString("source"),
        rs.getString("quote"),
        RoutineStatus.valueOf(rs.getString("status")),
        rs.getObject("created_at", OffsetDateTime.class));
  }

  public List<RoutineView> list(UUID workspace) {
    return jdbc.sql(
            "SELECT * FROM routines WHERE workspace_id=:workspace AND status<>'DISMISSED' ORDER BY created_at DESC")
        .param("workspace", workspace)
        .query(this::map)
        .list();
  }

  public boolean exists(UUID workspace, String title) {
    return jdbc.sql("SELECT count(*) FROM routines WHERE workspace_id=:workspace AND title=:title")
            .param("workspace", workspace)
            .param("title", title)
            .query(Long.class)
            .single()
        > 0;
  }

  public void insert(
      UUID workspace, String title, String schedule, String source, String quote, UUID messageId) {
    jdbc.sql(
            "INSERT INTO routines VALUES (:id,:workspace,:title,:schedule,:source,:quote,:message,'SUGGESTED',:now)")
        .param("id", UUID.randomUUID())
        .param("workspace", workspace)
        .param("title", title)
        .param("schedule", schedule)
        .param("source", source)
        .param("quote", quote)
        .param("message", messageId)
        .param("now", OffsetDateTime.now())
        .update();
  }

  public Optional<RoutineView> get(UUID workspace, UUID id) {
    return jdbc.sql("SELECT * FROM routines WHERE workspace_id=:workspace AND id=:id")
        .param("workspace", workspace)
        .param("id", id)
        .query(this::map)
        .optional();
  }

  public void status(UUID workspace, UUID id, RoutineStatus status) {
    jdbc.sql("UPDATE routines SET status=:status WHERE workspace_id=:workspace AND id=:id")
        .param("status", status.name())
        .param("workspace", workspace)
        .param("id", id)
        .update();
  }
}
