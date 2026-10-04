package id.remiyu.backend;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BackendIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;
  @Autowired JdbcClient jdbc;
  static final AtomicReference<String> mode = new AtomicReference<>("ok");
  static final AtomicInteger aiCalls = new AtomicInteger();
  static final HttpServer server;

  static {
    try {
      server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
      server.createContext(
          "/v1/extractions",
          exchange -> {
            aiCalls.incrementAndGet();
            var json = new ObjectMapper();
            var request = json.readTree(exchange.getRequestBody());
            byte[] body;
            int status = 200;
            if (mode.get().equals("unavailable")) {
              status = 503;
              body = "{}".getBytes(StandardCharsets.UTF_8);
            } else if (mode.get().equals("invalid")) {
              body = "{\"tasks\":null,\"agendas\":[]}".getBytes(StandardCharsets.UTF_8);
            } else {
              String id =
                  mode.get().equals("unknown")
                      ? UUID.randomUUID().toString()
                      : request.path("messages").get(0).path("id").asText();
              body =
                  json.writeValueAsBytes(
                      Map.of(
                          "tasks",
                          List.of(
                              Map.of(
                                  "title",
                                  "Kirim proposal",
                                  "priority",
                                  "MEDIUM",
                                  "sourceMessageIds",
                                  List.of(id))),
                          "agendas",
                          List.of(
                              Map.of(
                                  "title", "Rapat koordinasi", "sourceMessageIds", List.of(id)))));
            }
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
          });
      server.start();
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("remiyu.ai.base-url", () -> "http://127.0.0.1:" + server.getAddress().getPort());
  }

  @AfterAll
  static void stop() {
    server.stop(0);
  }

  @BeforeEach
  void reset() {
    for (String table :
        List.of(
            "task_submissions",
            "agenda_entries",
            "tasks",
            "import_runs",
            "chat_messages",
            "workspace_members",
            "workspaces",
            "auth_sessions",
            "app_users")) jdbc.sql("DELETE FROM " + table).update();
    mode.set("ok");
    aiCalls.set(0);
  }

  String token() throws Exception {
    return json(mvc.perform(
                post("/api/v1/auth/register")
                    .contentType("application/json")
                    .content(
                        mapper.writeValueAsString(
                            Map.of(
                                "name",
                                "Test User",
                                "email",
                                UUID.randomUUID() + "@example.com",
                                "password",
                                "TestPass123!"))))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString())
        .path("accessToken")
        .asText();
  }

  String workspace(String token) throws Exception {
    return json(mvc.perform(
                post("/api/v1/workspaces")
                    .header("Authorization", "Bearer " + token)
                    .contentType("application/json")
                    .content("{\"name\":\"Tim Test\",\"platform\":\"WHATSAPP\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.connectionStatus").value("NOT_CONNECTED"))
            .andReturn()
            .getResponse()
            .getContentAsString())
        .path("id")
        .asText();
  }

  JsonNode json(String raw) throws Exception {
    return mapper.readTree(raw);
  }

  MockMultipartFile file() {
    return new MockMultipartFile(
        "file",
        "chat.txt",
        "text/plain",
        "04/10/2026, 09:00 - Budi: Kirim proposal\n04/10/2026, 09:10 - Sari: Siap"
            .getBytes(StandardCharsets.UTF_8));
  }

  String imports(String workspace) {
    return "/api/v1/workspaces/" + workspace + "/imports";
  }

  @Test
  void authRejectsAnonymousAndRevokesSession() throws Exception {
    mvc.perform(get("/api/v1/workspaces")).andExpect(status().isUnauthorized());
    var token = token();
    mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
    mvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + token))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void taskSubmissionPersistsEvidenceAndChecksAccess() throws Exception {
    var admin = token();
    var outsider = token();
    var workspace = workspace(admin);
    String base = "/api/v1/workspaces/" + workspace + "/tasks";
    var task =
        json(mvc.perform(
                    post(base)
                        .header("Authorization", "Bearer " + admin)
                        .contentType("application/json")
                        .content("{\"title\":\"Kirim hasil\",\"priority\":\"MEDIUM\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString())
            .path("id")
            .asText();
    String path = base + "/" + task + "/submissions";
    mvc.perform(
            multipart(path)
                .param("link", "https://example.com/result")
                .header("Authorization", "Bearer " + outsider))
        .andExpect(status().isNotFound());
    mvc.perform(
            multipart(path)
                .param("link", "javascript:alert(1)")
                .header("Authorization", "Bearer " + admin))
        .andExpect(status().isBadRequest());
    assertThat(jdbc.sql("SELECT count(*) FROM task_submissions").query(Long.class).single())
        .isZero();
    mvc.perform(
            multipart(path)
                .file(
                    new MockMultipartFile(
                        "file",
                        "hasil.pdf",
                        "application/pdf",
                        "test evidence".getBytes(StandardCharsets.UTF_8)))
                .param("link", "https://example.com/result")
                .param("note", "Selesai")
                .header("Authorization", "Bearer " + admin))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.filename").value("hasil.pdf"));
    mvc.perform(get(base + "/" + task).header("Authorization", "Bearer " + admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DONE"));
    mvc.perform(get(path).header("Authorization", "Bearer " + admin))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].note").value("Selesai"));
    mvc.perform(get(path).header("Authorization", "Bearer " + outsider))
        .andExpect(status().isNotFound());
    assertThat(jdbc.sql("SELECT content FROM task_submissions").query(byte[].class).single())
        .isEqualTo("test evidence".getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void workspaceIsolationAndInvitation() throws Exception {
    var admin = token();
    var outsider = token();
    var workspace = workspace(admin);
    mvc.perform(
            get("/api/v1/workspaces/" + workspace).header("Authorization", "Bearer " + outsider))
        .andExpect(status().isNotFound());
    mvc.perform(
            multipart(imports(workspace))
                .file(file())
                .header("Authorization", "Bearer " + outsider))
        .andExpect(status().isNotFound());
    var invite =
        json(mvc.perform(
                    post("/api/v1/workspaces/" + workspace + "/invitation")
                        .header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString())
            .path("inviteToken")
            .asText();
    mvc.perform(
            post("/api/v1/workspaces/join/" + invite).header("Authorization", "Bearer " + outsider))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.memberCount").value(2));
    mvc.perform(
            multipart(imports(workspace))
                .file(file())
                .header("Authorization", "Bearer " + outsider))
        .andExpect(status().isNotFound());
  }

  @Test
  void secondWorkspaceRequiresPaymentAndNeverClaimsRealtime() throws Exception {
    var token = token();
    workspace(token);
    mvc.perform(
            post("/api/v1/workspaces")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("{\"name\":\"Tim Kedua\",\"platform\":\"TELEGRAM\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.billingStatus").value("PAYMENT_REQUIRED"))
        .andExpect(jsonPath("$.connectionStatus").value("NOT_CONNECTED"));
  }

  @Test
  void importCreatesDraftsAndReimportDoesNotCallAiAgain() throws Exception {
    var token = token();
    var workspace = workspace(token);
    var result =
        json(
            mvc.perform(
                    multipart(imports(workspace))
                        .file(file())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.newMessages").value(2))
                .andExpect(jsonPath("$.status").value("NEEDS_REVIEW"))
                .andReturn()
                .getResponse()
                .getContentAsString());
    String task = result.path("taskIds").get(0).asText();
    String base = "/api/v1/workspaces/" + workspace;
    mvc.perform(get(base + "/tasks").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].status").value("DRAFT"));
    mvc.perform(
            patch(base + "/tasks/" + task + "/status")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("{\"status\":\"DONE\"}"))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post(base + "/tasks/" + task + "/confirm").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("TODO"));
    mvc.perform(
            multipart(imports(workspace)).file(file()).header("Authorization", "Bearer " + token))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.newMessages").value(0))
        .andExpect(jsonPath("$.duplicateMessages").value(2))
        .andExpect(jsonPath("$.taskIds").isEmpty());
    assertThat(aiCalls.get()).isEqualTo(1);
    var agenda = result.path("agendaIds").get(0).asText();
    mvc.perform(
            post(base + "/agendas/" + agenda + "/confirm")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CONFIRMED"));
  }

  @Test
  void failedAiRollsBackMessagesAndCanRetry() throws Exception {
    var token = token();
    var workspace = workspace(token);
    mode.set("unavailable");
    mvc.perform(
            multipart(imports(workspace)).file(file()).header("Authorization", "Bearer " + token))
        .andExpect(status().isBadGateway());
    assertThat(jdbc.sql("SELECT count(*) FROM chat_messages").query(Long.class).single()).isZero();
    assertThat(jdbc.sql("SELECT count(*) FROM import_runs").query(Long.class).single()).isZero();
    mode.set("ok");
    mvc.perform(
            multipart(imports(workspace)).file(file()).header("Authorization", "Bearer " + token))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.newMessages").value(2));
  }

  @Test
  void inventedSourcesAndInvalidContractAreRejected() throws Exception {
    var token = token();
    var workspace = workspace(token);
    for (String invalid : List.of("unknown", "invalid")) {
      mode.set(invalid);
      mvc.perform(
              multipart(imports(workspace)).file(file()).header("Authorization", "Bearer " + token))
          .andExpect(status().isBadGateway());
      assertThat(jdbc.sql("SELECT count(*) FROM tasks").query(Long.class).single()).isZero();
    }
  }

  @Test
  void validatesRequestAndTaskFilters() throws Exception {
    var token = token();
    var workspace = workspace(token);
    mvc.perform(
            get("/api/v1/workspaces/" + workspace + "/tasks?limit=0")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/v1/workspaces/" + workspace + "/tasks")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("{\"title\":\"\",\"priority\":\"UNKNOWN\"}"))
        .andExpect(status().isBadRequest());
  }
}
