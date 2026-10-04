package id.remiyu.backend.model;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record UserAccount(UUID id, String name, String email, String passwordHash) {}
