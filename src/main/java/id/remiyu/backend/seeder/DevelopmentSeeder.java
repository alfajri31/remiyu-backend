package id.remiyu.backend.seeder;

import id.remiyu.backend.repository.AuthRepository;
import id.remiyu.backend.repository.TaskRepository;
import id.remiyu.backend.service.AuthService;
import id.remiyu.backend.service.WorkspaceService;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "remiyu.seed.enabled", havingValue = "true")
public class DevelopmentSeeder implements ApplicationRunner {
  private final AuthRepository accounts;
  private final AuthService auth;
  private final WorkspaceService workspaces;
  private final TaskRepository tasks;
  private final String email, password;

  public DevelopmentSeeder(
      AuthRepository accounts,
      AuthService auth,
      WorkspaceService workspaces,
      TaskRepository tasks,
      @Value("${remiyu.seed.email}") String email,
      @Value("${remiyu.seed.password}") String password) {
    this.accounts = accounts;
    this.auth = auth;
    this.workspaces = workspaces;
    this.tasks = tasks;
    this.email = email;
    this.password = password;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (password.length() < 8 || password.length() > 72)
      throw new IllegalStateException("SEED_PASSWORD wajib 8-72 karakter saat seeder aktif.");
    if (accounts.byEmail(email).isPresent()) return;
    var session = auth.register("Admin Demo", email, password);
    var workspace = workspaces.create(session.user().id(), "Workspace Demo", "WHATSAPP", null);
    tasks.create(
        workspace.id(),
        null,
        "Coba import chat untuk meninjau draft AI",
        "Data demo seeder",
        null,
        null,
        "MEDIUM",
        "TODO",
        List.of());
    auth.logout(session.accessToken());
  }
}
