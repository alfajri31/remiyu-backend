package id.remiyu.backend.service;

import id.remiyu.backend.dto.response.AuthResponse;

public interface AuthService {
  AuthResponse register(String name, String email, String password);

  AuthResponse login(String email, String password);

  void logout(String token);
}
