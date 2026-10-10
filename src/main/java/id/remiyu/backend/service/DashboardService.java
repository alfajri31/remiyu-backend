package id.remiyu.backend.service;

import id.remiyu.backend.dto.response.DashboardView;
import java.util.UUID;

public interface DashboardService {
  DashboardView get(UUID userId);
}
