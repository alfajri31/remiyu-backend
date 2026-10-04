package id.remiyu.backend.repository;

import id.remiyu.backend.dto.response.SubmissionView;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class SubmissionRepository {
  private final JdbcClient jdbc;

  public SubmissionRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public SubmissionView save(
      UUID task, UUID user, String link, String note, String filename, byte[] content) {
    UUID id = UUID.randomUUID();
    OffsetDateTime now = OffsetDateTime.now();
    jdbc.sql(
            "INSERT INTO task_submissions (id,task_id,user_id,link,note,filename,content,created_at) VALUES (:id,:task,:user,:link,:note,:filename,:content,:now)")
        .param("id", id)
        .param("task", task)
        .param("user", user)
        .param("link", link)
        .param("note", note)
        .param("filename", filename)
        .param("content", content)
        .param("now", now)
        .update();
    return new SubmissionView(id, task, user, link, note, filename, now);
  }

  public List<SubmissionView> list(UUID task) {
    return jdbc.sql(
            "SELECT id,task_id,user_id,link,note,filename,created_at FROM task_submissions WHERE task_id=:task ORDER BY created_at DESC")
        .param("task", task)
        .query(
            (rs, n) ->
                new SubmissionView(
                    rs.getObject("id", UUID.class),
                    rs.getObject("task_id", UUID.class),
                    rs.getObject("user_id", UUID.class),
                    rs.getString("link"),
                    rs.getString("note"),
                    rs.getString("filename"),
                    rs.getObject("created_at", OffsetDateTime.class)))
        .list();
  }
}
