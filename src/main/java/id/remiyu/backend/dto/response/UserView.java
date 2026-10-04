package id.remiyu.backend.dto.response;

import java.util.UUID;

public record UserView(UUID id, String name, String email) {}
