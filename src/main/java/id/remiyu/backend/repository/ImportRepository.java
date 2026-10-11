package id.remiyu.backend.repository;

import id.remiyu.backend.dto.ai.AiContracts;
import id.remiyu.backend.dto.response.ImportRunView;
import id.remiyu.backend.entity.ChatMessageEntity;
import id.remiyu.backend.model.ParsedChatMessage;
import id.remiyu.backend.model.enums.ImportStatus;
import id.remiyu.backend.repository.jpa.ChatMessageJpaRepository;
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
  private final ChatMessageJpaRepository chatMessages;

  public void lockChannel(UUID channel) {
    jdbc.sql("SELECT id FROM channels WHERE id=:id FOR UPDATE")
        .param("id", channel)
        .query(UUID.class)
        .single();
  }

  public long nextMessageOrder(UUID channel) {
    return jdbc.sql(
            "SELECT COALESCE(MAX(message_order), 0) + 1 FROM chat_messages WHERE channel_id=:channel")
        .param("channel", channel)
        .query(Long.class)
        .single();
  }

  public boolean contains(UUID channel, String hash) {
    return chatMessages.existsByChannelIdAndMessageHash(channel, hash);
  }

  public UUID message(
      UUID channel, UUID importId, long messageOrder, String hash, ParsedChatMessage m) {
    UUID id = UUID.randomUUID();
    chatMessages.save(
        ChatMessageEntity.builder()
            .id(id)
            .channelId(channel)
            .importId(importId)
            .messageHash(hash)
            .messageOrder(messageOrder)
            .sender(m.sender())
            .sentAt(m.sentAt())
            .content(m.text())
            .build());
    return id;
  }

  public void startRun(UUID id, UUID channel, String filename, String provider) {
    jdbc.sql(
            "INSERT INTO import_runs VALUES (:id,:w,:filename,:added,:duplicates,:provider,:status,:now)")
        .param("id", id)
        .param("w", channel)
        .param("filename", filename)
        .param("added", 0)
        .param("duplicates", 0)
        .param("provider", provider)
        .param("status", ImportStatus.NEEDS_REVIEW.name())
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

  public List<ImportRunView> list(UUID channel) {
    return jdbc.sql(
            "SELECT * FROM import_runs WHERE channel_id=:w ORDER BY created_at DESC LIMIT 100")
        .param("w", channel)
        .query(
            (rs, n) ->
                new ImportRunView(
                    rs.getObject("id", UUID.class),
                    rs.getObject("channel_id", UUID.class),
                    rs.getString("filename"),
                    rs.getInt("new_messages"),
                    rs.getInt("duplicate_messages"),
                    rs.getString("ai_provider"),
                    ImportStatus.valueOf(rs.getString("status")),
                    rs.getObject("created_at", OffsetDateTime.class)))
        .list();
  }

  public List<AiContracts.Message> recentMessages(UUID channel) {
    return jdbc.sql(
            "SELECT id,sender,sent_at,content FROM chat_messages WHERE channel_id=:channel ORDER BY sent_at DESC NULLS LAST, message_order DESC LIMIT 100")
        .param("channel", channel)
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
