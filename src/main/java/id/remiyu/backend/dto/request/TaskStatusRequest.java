package id.remiyu.backend.dto.request;

import id.remiyu.backend.model.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record TaskStatusRequest(@NotNull TaskStatus status) {}
