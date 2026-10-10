package id.remiyu.backend.service;

import id.remiyu.backend.dto.response.WorkspaceSummaryView;
import java.util.UUID;

public interface WorkspaceSummaryService {
  WorkspaceSummaryView generate(UUID user, UUID workspace);
}
