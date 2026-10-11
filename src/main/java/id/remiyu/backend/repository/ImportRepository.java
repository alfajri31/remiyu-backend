package id.remiyu.backend.repository;

import id.remiyu.backend.dto.ai.AiContracts;
import id.remiyu.backend.dto.response.ImportRunView;
import id.remiyu.backend.model.ParsedChatMessage;
import id.remiyu.backend.model.enums.ImportStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ImportRepository {
  private final JdbcClient jdbc;

  public void lockWorkspace(UUID workspace) {
    jdbc.sql("SELECT id FROM workspaces WHERE id=:id FOR UPDATE")
        .param("id", workspace)
        .query(UUID.class)
        .single();
  }

  public boolean contains(UUID workspace, String key) {
    return jdbc.sql("SELECT count(*) FROM chat_messages WHERE workspace_id=:w AND message_key=:key")
            .param("w", workspace)
            .param("key", key)
            .query(Long.class)
            .single()
        > 0;
  }

  public UUID message(UUID workspace, UUID importId, String key, ParsedChatMessage m) {
    UUID id = UUID.randomUUID();
    jdbc.sql(
            "INSERT INTO chat_messages (id,workspace_id,import_id,message_key,sender,sent_at,content) "
                + "VALUES (:id,:w,:import,:key,:sender,:sent,:content)")
        .param("id", id)
        .param("w", workspace)
        .param("import", importId)
        .param("key", key)
        .param("sender", m.sender())
        .param("sent", m.sentAt())
        .param("content", m.text())
        .update();
    return id;
  }

  public void startRun(UUID id, UUID workspace, String filename, String provider) {
    jdbc.sql(
            "INSERT INTO import_runs VALUES (:id,:w,:filename,:added,:duplicates,:provider,:status,:now)")
        .param("id", id)
        .param("w", workspace)
        .param("filename", filename)
        .param("added", 0)
        .param("duplicates", 0)
        .param("provider", provider)
        .param(
            "status", ImportStatus.NEEDS_REVIEW.name())
        .param("now", OffsetDateTime.now())
        .update();
  }

  public void completeRun(UUID id, int added, int duplicates) {
    jdbc.sql(
            "UPDATE import_runs SET new_messages=:added,duplicate_messages=:duplicates,status=:status WHERE id=:id")
        .param("id", id)
        .param("added", added)
        .param("duplicates", duplicates)
        .param(
            "status",
            (added == 0 ? ImportStatus.NO_NEW_MESSAGES : ImportStatus.NEEDS_REVIEW).name())
        .update();
  }

  public List<ImportRunView> list(UUID workspace) {
    return jdbc.sql(
            "SELECT * FROM import_runs WHERE workspace_id=:w ORDER BY created_at DESC LIMIT 100")
        .param("w", workspace)
        .query(
            (rs, n) ->
                new ImportRunView(
                    rs.getObject("id", UUID.class),
                    rs.getObject("workspace_id", UUID.class),
                    rs.getString("filename"),
                    rs.getInt("new_messages"),
                    rs.getInt("duplicate_messages"),
                    rs.getString("ai_provider"),
                    ImportStatus.valueOf(rs.getString("status")),
                    rs.getObject("created_at", OffsetDateTime.class)))
        .list();
  }

  public List<AiContracts.Message> recentMessages(UUID workspace) {
    return jdbc.sql(
            "SELECT id,sender,sent_at,content FROM chat_messages WHERE workspace_id=:workspace ORDER BY sent_at DESC LIMIT 100")
        .param("workspace", workspace)
        .query(
            (rs, row) ->
                new AiContracts.Message(
                    rs.getObject("id", UUID.class),
                    rs.getString("sender"),
                    rs.getObject("sent_at", OffsetDateTime.class),
                    rs.getString("content")))
        .list();
  }
}
