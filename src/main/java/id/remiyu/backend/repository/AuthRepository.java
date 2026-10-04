package id.remiyu.backend.repository;

import id.remiyu.backend.dto.response.UserView;
import id.remiyu.backend.model.UserAccount;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class AuthRepository {
  private final JdbcClient jdbc;

  public AuthRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public Optional<UserAccount> byEmail(String email) {
    return jdbc.sql("SELECT id,name,email,password_hash FROM app_users WHERE email=:email")
        .param("email", email)
        .query(
            (rs, n) ->
                new UserAccount(
                    rs.getObject("id", UUID.class),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password_hash")))
        .optional();
  }

  public void insert(UUID id, String name, String email, String hash) {
    jdbc.sql("INSERT INTO app_users VALUES (:id,:name,:email,:hash,:now)")
        .param("id", id)
        .param("name", name)
        .param("email", email)
        .param("hash", hash)
        .param("now", OffsetDateTime.now())
        .update();
  }

  public void session(String hash, UUID userId, OffsetDateTime expires) {
    jdbc.sql("INSERT INTO auth_sessions VALUES (:hash,:user,:expires)")
        .param("hash", hash)
        .param("user", userId)
        .param("expires", expires)
        .update();
  }

  public Optional<UserView> sessionUser(String hash) {
    return jdbc.sql(
            "SELECT u.id,u.name,u.email FROM app_users u JOIN auth_sessions s ON s.user_id=u.id WHERE s.token_hash=:hash AND s.expires_at>:now")
        .param("hash", hash)
        .param("now", OffsetDateTime.now())
        .query(
            (rs, n) ->
                new UserView(
                    rs.getObject("id", UUID.class), rs.getString("name"), rs.getString("email")))
        .optional();
  }

  public void revoke(String hash) {
    jdbc.sql("DELETE FROM auth_sessions WHERE token_hash=:hash").param("hash", hash).update();
  }
}
