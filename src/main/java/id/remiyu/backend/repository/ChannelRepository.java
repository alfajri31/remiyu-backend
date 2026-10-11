package id.remiyu.backend.repository;

import id.remiyu.backend.dto.response.ChannelView;
import id.remiyu.backend.dto.response.MemberView;
import id.remiyu.backend.model.enums.BillingStatus;
import id.remiyu.backend.model.enums.ChannelRole;
import id.remiyu.backend.model.enums.ConnectionStatus;
import id.remiyu.backend.model.enums.Platform;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChannelRepository {
  private final JdbcClient jdbc;

  private static final String SELECT =
      "SELECT w.*, (SELECT count(*) FROM channels_members m WHERE m.channel_id=w.id) member_count FROM channels w ";

  private ChannelView map(ResultSet rs, int n) throws SQLException {
    return new ChannelView(
        rs.getObject("id", UUID.class),
        rs.getString("name"),
        Platform.valueOf(rs.getString("platform")),
        rs.getString("account"),
        ConnectionStatus.valueOf(rs.getString("connection_status")),
        BillingStatus.valueOf(rs.getString("billing_status")),
        rs.getLong("member_count"),
        rs.getObject("created_at", OffsetDateTime.class));
  }

  public List<ChannelView> list(UUID user) {
    return jdbc.sql(
            SELECT
                + "JOIN channels_members m ON m.channel_id=w.id WHERE m.user_id=:user ORDER BY w.created_at")
        .param("user", user)
        .query(this::map)
        .list();
  }

  public boolean member(UUID id, UUID user) {
    return jdbc.sql("SELECT count(*) FROM channels_members WHERE channel_id=:id AND user_id=:user")
            .param("id", id)
            .param("user", user)
            .query(Long.class)
            .single()
        > 0;
  }

  public boolean owner(UUID id, UUID user) {
    return jdbc.sql("SELECT count(*) FROM channels WHERE id=:id AND owner_id=:user")
            .param("id", id)
            .param("user", user)
            .query(Long.class)
            .single()
        > 0;
  }

  public void lockOwner(UUID user) {
    jdbc.sql("SELECT id FROM app_users WHERE id=:id FOR UPDATE")
        .param("id", user)
        .query(UUID.class)
        .single();
  }

  public long ownedCount(UUID user) {
    return jdbc.sql("SELECT count(*) FROM channels WHERE owner_id=:user")
        .param("user", user)
        .query(Long.class)
        .single();
  }

  public void insert(
      UUID id, UUID owner, String name, Platform platform, String account, BillingStatus billing) {
    jdbc.sql(
            "INSERT INTO channels VALUES (:id,:owner,:name,:platform,:account,:connection,:billing,:invite,:now)")
        .param("id", id)
        .param("owner", owner)
        .param("name", name)
        .param("platform", platform.name())
        .param("account", account)
        .param("connection", ConnectionStatus.NOT_CONNECTED.name())
        .param("billing", billing.name())
        .param("invite", UUID.randomUUID())
        .param("now", OffsetDateTime.now())
        .update();
    addMember(id, owner, ChannelRole.ADMIN);
  }

  public void addMember(UUID id, UUID user, ChannelRole role) {
    jdbc.sql("INSERT INTO channels_members VALUES (:id,:user,:role)")
        .param("id", id)
        .param("user", user)
        .param("role", role.name())
        .update();
  }

  public ChannelView get(UUID id) {
    return jdbc.sql(SELECT + "WHERE w.id=:id").param("id", id).query(this::map).single();
  }

  public UUID invite(UUID id) {
    return jdbc.sql("SELECT invite_token FROM channels WHERE id=:id")
        .param("id", id)
        .query(UUID.class)
        .single();
  }

  public java.util.Optional<UUID> invitedChannel(UUID token) {
    return jdbc.sql("SELECT id FROM channels WHERE invite_token=:token FOR UPDATE")
        .param("token", token)
        .query(UUID.class)
        .optional();
  }

  public List<MemberView> members(UUID id) {
    return jdbc.sql(
            "SELECT u.id,u.name,u.email,m.role FROM app_users u JOIN channels_members m ON m.user_id=u.id WHERE m.channel_id=:id ORDER BY u.name")
        .param("id", id)
        .query(
            (rs, n) ->
                new MemberView(
                    rs.getObject("id", UUID.class),
                    rs.getString("name"),
                    rs.getString("email"),
                    ChannelRole.valueOf(rs.getString("role"))))
        .list();
  }
}
