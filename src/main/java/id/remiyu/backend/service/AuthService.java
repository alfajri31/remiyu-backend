package id.remiyu.backend.service;

import id.remiyu.backend.common.exception.ApiException;
import id.remiyu.backend.common.util.Hashes;
import id.remiyu.backend.dto.response.AuthResponse;
import id.remiyu.backend.dto.response.UserView;
import id.remiyu.backend.repository.AuthRepository;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final AuthRepository repository;
  private final PasswordEncoder encoder;
  private final long tokenHours;
  private final SecureRandom random = new SecureRandom();

  public AuthService(
      AuthRepository repository,
      PasswordEncoder encoder,
      @Value("${remiyu.auth.token-hours}") long tokenHours) {
    this.repository = repository;
    this.encoder = encoder;
    this.tokenHours = tokenHours;
  }

  @Transactional
  public AuthResponse register(String name, String email, String password) {
    email = email.trim().toLowerCase(Locale.ROOT);
    if (repository.byEmail(email).isPresent())
      throw new ApiException(HttpStatus.CONFLICT, "Email sudah terdaftar.");
    UUID id = UUID.randomUUID();
    repository.insert(id, name.trim(), email, encoder.encode(password));
    return issue(new UserView(id, name.trim(), email));
  }

  @Transactional
  public AuthResponse login(String email, String password) {
    var account =
        repository
            .byEmail(email.trim().toLowerCase(Locale.ROOT))
            .orElseThrow(
                () -> new ApiException(HttpStatus.UNAUTHORIZED, "Email atau password salah."));
    if (!encoder.matches(password, account.passwordHash()))
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Email atau password salah.");
    return issue(new UserView(account.id(), account.name(), account.email()));
  }

  private AuthResponse issue(UserView user) {
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    var expires = OffsetDateTime.now().plusHours(tokenHours);
    repository.session(Hashes.sha256(token), user.id(), expires);
    return new AuthResponse(token, "Bearer", expires, user);
  }

  public void logout(String token) {
    repository.revoke(Hashes.sha256(token));
  }
}
