package id.remiyu.backend.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.remiyu.backend.dto.response.TaskView;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class TaskRepository {
  private final JdbcClient jdbc;
  private final ObjectMapper mapper;

  public TaskRepository(JdbcClient jdbc, ObjectMapper mapper) {
    this.jdbc = jdbc;
    this.mapper = mapper;
  }

  private TaskView map(ResultSet rs, int n) throws SQLException {
    try {
      return new TaskView(
          rs.getObject("id", UUID.class),
          rs.getObject("workspace_id", UUID.class),
          rs.getObject("import_id", UUID.class),
          rs.getString("title"),
          rs.getString("description"),
          rs.getString("assignee_name"),
          rs.getObject("due_at", OffsetDateTime.class),
          rs.getString("priority"),
          rs.getString("status"),
          rs.getBoolean("submitted"),
          mapper.readValue(rs.getString("source_message_ids"), new TypeReference<List<UUID>>() {}),
          rs.getObject("created_at", OffsetDateTime.class),
          rs.getObject("updated_at", OffsetDateTime.class));
    } catch (java.io.IOException e) {
      throw new SQLException("Invalid stored source ids", e);
    }
  }

  public List<TaskView> list(UUID workspace, String status, int limit, int offset) {
    return jdbc.sql(
            "SELECT tasks.*, EXISTS (SELECT 1 FROM task_submissions s WHERE s.task_id=tasks.id) AS submitted FROM tasks WHERE workspace_id=:w"
                + (status == null ? "" : " AND status=:status")
                + " ORDER BY created_at DESC,id LIMIT :limit OFFSET :offset")
        .param("w", workspace)
        .params(status == null ? java.util.Map.of() : java.util.Map.of("status", status))
        .param("limit", limit)
        .param("offset", offset)
        .query(this::map)
        .list();
  }

  public Optional<TaskView> get(UUID workspace, UUID id) {
    return jdbc.sql(
            "SELECT tasks.*, EXISTS (SELECT 1 FROM task_submissions s WHERE s.task_id=tasks.id) AS submitted FROM tasks WHERE workspace_id=:w AND id=:id")
        .param("w", workspace)
        .param("id", id)
        .query(this::map)
        .optional();
  }

  public UUID create(
      UUID workspace,
      UUID importId,
      String title,
      String description,
      String assignee,
      OffsetDateTime due,
      String priority,
      String status,
      List<UUID> sources) {
    UUID id = UUID.randomUUID();
    try {
      jdbc.sql(
              "INSERT INTO tasks VALUES (:id,:w,:import,:title,:description,:assignee,:due,:priority,:status,:sources,:now,:now)")
          .param("id", id)
          .param("w", workspace)
          .param("import", importId)
          .param("title", title)
          .param("description", description)
          .param("assignee", assignee)
          .param("due", due)
          .param("priority", priority)
          .param("status", status)
          .param("sources", mapper.writeValueAsString(sources))
          .param("now", OffsetDateTime.now())
          .update();
      return id;
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  public void update(
      UUID workspace,
      UUID id,
      String title,
      String description,
      String assignee,
      OffsetDateTime due,
      String priority,
      String status) {
    jdbc.sql(
            "UPDATE tasks SET title=:title,description=:description,assignee_name=:assignee,due_at=:due,priority=:priority,status=:status,updated_at=:now WHERE id=:id AND workspace_id=:w")
        .param("title", title)
        .param("description", description)
        .param("assignee", assignee)
        .param("due", due)
        .param("priority", priority)
        .param("status", status)
        .param("now", OffsetDateTime.now())
        .param("id", id)
        .param("w", workspace)
        .update();
  }

  public void delete(UUID workspace, UUID id) {
    jdbc.sql("DELETE FROM tasks WHERE id=:id AND workspace_id=:w")
        .param("id", id)
        .param("w", workspace)
        .update();
  }
}
