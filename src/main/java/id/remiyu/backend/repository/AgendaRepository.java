package id.remiyu.backend.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.remiyu.backend.dto.response.AgendaView;
import id.remiyu.backend.model.enums.AgendaStatus;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AgendaRepository {
  private final JdbcClient jdbc;
  private final ObjectMapper mapper;

  private AgendaView map(ResultSet rs, int n) throws SQLException {
    try {
      return new AgendaView(
          rs.getObject("id", UUID.class),
          rs.getString("title"),
          rs.getObject("starts_at", OffsetDateTime.class),
          AgendaStatus.valueOf(rs.getString("status")),
          mapper.readValue(rs.getString("source_message_ids"), new TypeReference<List<UUID>>() {}));
    } catch (java.io.IOException e) {
      throw new SQLException(e);
    }
  }

  public List<AgendaView> list(UUID workspace, int limit, long offset) {
    return jdbc.sql(
            "SELECT * FROM agenda_entries WHERE workspace_id=:w ORDER BY starts_at NULLS LAST,id LIMIT :limit OFFSET :offset")
        .param("w", workspace)
        .param("limit", limit)
        .param("offset", offset)
        .query(this::map)
        .list();
  }

  public long count(UUID workspace) {
    return jdbc.sql("SELECT count(*) FROM agenda_entries WHERE workspace_id=:w")
        .param("w", workspace)
        .query(Long.class)
        .single();
  }

  public Optional<AgendaView> get(UUID workspace, UUID id) {
    return jdbc.sql("SELECT * FROM agenda_entries WHERE workspace_id=:w AND id=:id")
        .param("w", workspace)
        .param("id", id)
        .query(this::map)
        .optional();
  }

  public void edit(UUID workspace, UUID id, String title, OffsetDateTime starts) {
    jdbc.sql(
            "UPDATE agenda_entries SET title=:title,starts_at=:starts WHERE workspace_id=:w AND id=:id")
        .param("title", title)
        .param("starts", starts)
        .param("w", workspace)
        .param("id", id)
        .update();
  }

  public void confirm(UUID workspace, UUID id) {
    jdbc.sql("UPDATE agenda_entries SET status=:status WHERE workspace_id=:w AND id=:id")
        .param("status", AgendaStatus.CONFIRMED.name())
        .param("w", workspace)
        .param("id", id)
        .update();
  }

  public void delete(UUID workspace, UUID id) {
    jdbc.sql("DELETE FROM agenda_entries WHERE workspace_id=:w AND id=:id")
        .param("w", workspace)
        .param("id", id)
        .update();
  }
}
