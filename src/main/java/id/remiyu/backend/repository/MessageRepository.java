package id.remiyu.backend.repository;

import id.remiyu.backend.dto.response.SourceMessageView;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class MessageRepository {
  private final JdbcClient jdbc;

  public MessageRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public List<SourceMessageView> sources(UUID workspace, List<UUID> ids) {
    return jdbc.sql(
            "SELECT id,sender,sent_at,content FROM chat_messages WHERE workspace_id=:w AND id IN (:ids) ORDER BY sent_at")
        .param("w", workspace)
        .param("ids", ids)
        .query(
            (rs, n) ->
                new SourceMessageView(
                    rs.getObject("id", UUID.class),
                    rs.getString("sender"),
                    rs.getObject("sent_at", OffsetDateTime.class),
                    rs.getString("content")))
        .list();
  }
}
