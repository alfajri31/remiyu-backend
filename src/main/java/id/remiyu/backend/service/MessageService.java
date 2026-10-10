package id.remiyu.backend.service;

import id.remiyu.backend.dto.response.SourceMessageView;
import java.util.List;
import java.util.UUID;

public interface MessageService {
  List<SourceMessageView> sources(UUID user, UUID workspace, List<UUID> ids);
}
