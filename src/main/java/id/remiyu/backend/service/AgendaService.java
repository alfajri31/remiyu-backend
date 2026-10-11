package id.remiyu.backend.service;

import id.remiyu.backend.dto.response.AgendaView;
import id.remiyu.backend.dto.response.PageResponse;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface AgendaService {
  PageResponse<AgendaView> list(UUID user, UUID channel, Pageable pageable);

  AgendaView edit(UUID user, UUID channel, UUID id, String title, OffsetDateTime starts);

  AgendaView confirm(UUID user, UUID channel, UUID id);

  void delete(UUID user, UUID channel, UUID id);
}
