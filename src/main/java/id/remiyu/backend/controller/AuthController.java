package id.remiyu.backend.controller;

import id.remiyu.backend.dto.request.LoginRequest;
import id.remiyu.backend.dto.request.RegisterRequest;
import id.remiyu.backend.dto.response.AuthResponse;
import id.remiyu.backend.dto.response.UserView;
import id.remiyu.backend.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final AuthService service;

  public AuthController(AuthService service) {
    this.service = service;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public AuthResponse register(@Valid @RequestBody RegisterRequest body) {
    return service.register(body.name(), body.email(), body.password());
  }

  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody LoginRequest body) {
    return service.login(body.email(), body.password());
  }

  @GetMapping("/me")
  public UserView me(@AuthenticationPrincipal UserView user) {
    return user;
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(@RequestHeader("Authorization") String header) {
    service.logout(header.substring(7));
  }
}
