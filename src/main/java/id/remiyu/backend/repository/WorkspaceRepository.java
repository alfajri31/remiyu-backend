package id.remiyu.backend.repository;

import id.remiyu.backend.dto.response.MemberView;
import id.remiyu.backend.dto.response.WorkspaceView;
import id.remiyu.backend.model.enums.BillingStatus;
import id.remiyu.backend.model.enums.ConnectionStatus;
import id.remiyu.backend.model.enums.Platform;
import id.remiyu.backend.model.enums.WorkspaceRole;
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
public class WorkspaceRepository {
  private final JdbcClient jdbc;

  private static final String SELECT =
      "SELECT w.*, (SELECT count(*) FROM workspace_members m WHERE m.workspace_id=w.id) member_count FROM workspaces w ";

  private WorkspaceView map(ResultSet rs, int n) throws SQLException {
    return new WorkspaceView(
        rs.getObject("id", UUID.class),
        rs.getString("name"),
        Platform.valueOf(rs.getString("platform")),
        rs.getString("account"),
        ConnectionStatus.valueOf(rs.getString("connection_status")),
        BillingStatus.valueOf(rs.getString("billing_status")),
        rs.getLong("member_count"),
        rs.getObject("created_at", OffsetDateTime.class));
  }

  public List<WorkspaceView> list(UUID user) {
    return jdbc.sql(
            SELECT
                + "JOIN workspace_members m ON m.workspace_id=w.id WHERE m.user_id=:user ORDER BY w.created_at")
        .param("user", user)
        .query(this::map)
        .list();
  }

  public boolean member(UUID id, UUID user) {
    return jdbc.sql(
                "SELECT count(*) FROM workspace_members WHERE workspace_id=:id AND user_id=:user")
            .param("id", id)
            .param("user", user)
            .query(Long.class)
            .single()
        > 0;
  }

  public boolean owner(UUID id, UUID user) {
    return jdbc.sql("SELECT count(*) FROM workspaces WHERE id=:id AND owner_id=:user")
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
    return jdbc.sql("SELECT count(*) FROM workspaces WHERE owner_id=:user")
        .param("user", user)
        .query(Long.class)
        .single();
  }

  public void insert(
      UUID id, UUID owner, String name, Platform platform, String account, BillingStatus billing) {
    jdbc.sql(
            "INSERT INTO workspaces VALUES (:id,:owner,:name,:platform,:account,:connection,:billing,:invite,:now)")
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
    addMember(id, owner, WorkspaceRole.ADMIN);
  }

  public void addMember(UUID id, UUID user, WorkspaceRole role) {
    jdbc.sql("INSERT INTO workspace_members VALUES (:id,:user,:role)")
        .param("id", id)
        .param("user", user)
        .param("role", role.name())
        .update();
  }

  public WorkspaceView get(UUID id) {
    return jdbc.sql(SELECT + "WHERE w.id=:id").param("id", id).query(this::map).single();
  }

  public UUID invite(UUID id) {
    return jdbc.sql("SELECT invite_token FROM workspaces WHERE id=:id")
        .param("id", id)
        .query(UUID.class)
        .single();
  }

  public java.util.Optional<UUID> invitedWorkspace(UUID token) {
    return jdbc.sql("SELECT id FROM workspaces WHERE invite_token=:token FOR UPDATE")
        .param("token", token)
        .query(UUID.class)
        .optional();
  }

  public List<MemberView> members(UUID id) {
    return jdbc.sql(
            "SELECT u.id,u.name,u.email,m.role FROM app_users u JOIN workspace_members m ON m.user_id=u.id WHERE m.workspace_id=:id ORDER BY u.name")
        .param("id", id)
        .query(
            (rs, n) ->
                new MemberView(
                    rs.getObject("id", UUID.class),
                    rs.getString("name"),
                    rs.getString("email"),
                    WorkspaceRole.valueOf(rs.getString("role"))))
        .list();
  }
}
