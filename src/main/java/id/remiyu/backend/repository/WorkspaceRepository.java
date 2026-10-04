package id.remiyu.backend.repository;

import id.remiyu.backend.dto.response.MemberView;
import id.remiyu.backend.dto.response.WorkspaceView;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class WorkspaceRepository {
  private final JdbcClient jdbc;

  public WorkspaceRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  private static final String SELECT =
      "SELECT w.*, (SELECT count(*) FROM workspace_members m WHERE m.workspace_id=w.id) member_count FROM workspaces w ";

  private WorkspaceView map(ResultSet rs, int n) throws SQLException {
    return new WorkspaceView(
        rs.getObject("id", UUID.class),
        rs.getString("name"),
        rs.getString("platform"),
        rs.getString("account"),
        rs.getString("connection_status"),
        rs.getString("billing_status"),
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
      UUID id, UUID owner, String name, String platform, String account, String billing) {
    jdbc.sql(
            "INSERT INTO workspaces VALUES (:id,:owner,:name,:platform,:account,'NOT_CONNECTED',:billing,:invite,:now)")
        .param("id", id)
        .param("owner", owner)
        .param("name", name)
        .param("platform", platform)
        .param("account", account)
        .param("billing", billing)
        .param("invite", UUID.randomUUID())
        .param("now", OffsetDateTime.now())
        .update();
    addMember(id, owner, "ADMIN");
  }

  public void addMember(UUID id, UUID user, String role) {
    jdbc.sql("INSERT INTO workspace_members VALUES (:id,:user,:role)")
        .param("id", id)
        .param("user", user)
        .param("role", role)
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
                    rs.getString("role")))
        .list();
  }
}
