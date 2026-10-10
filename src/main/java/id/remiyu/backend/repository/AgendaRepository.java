package id.remiyu.backend.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.remiyu.backend.dto.response.AgendaView;
import id.remiyu.backend.entity.AgendaEntity;
import id.remiyu.backend.model.enums.AgendaStatus;
import id.remiyu.backend.repository.jpa.AgendaJpaRepository;
import id.remiyu.backend.repository.specification.AgendaSpecifications;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AgendaRepository {
  private final AgendaJpaRepository agendas;
  private final ObjectMapper mapper;

  public Page<AgendaView> list(UUID workspace, Pageable pageable) {
    Pageable sorted =
        PageRequest.of(
            pageable.getPageNumber(), pageable.getPageSize(), Sort.by("startsAt").ascending());
    return agendas.findAll(AgendaSpecifications.workspaceId(workspace), sorted).map(this::view);
  }

  public Optional<AgendaView> get(UUID workspace, UUID id) {
    return agendas.findByIdAndWorkspaceId(id, workspace).map(this::view);
  }

  public UUID create(
      UUID workspace, UUID importId, String title, OffsetDateTime startsAt, List<UUID> sources) {
    try {
      return agendas
          .save(
              AgendaEntity.builder()
                  .id(UUID.randomUUID())
                  .workspaceId(workspace)
                  .importId(importId)
                  .title(title)
                  .startsAt(startsAt)
                  .status(AgendaStatus.DRAFT)
                  .sourceMessageIds(mapper.writeValueAsString(sources))
                  .chatMessageId(sources.isEmpty() ? null : sources.getFirst())
                  .reviewStatus(id.remiyu.backend.model.enums.ReviewStatus.PENDING)
                  .createdAt(OffsetDateTime.now())
                  .build())
          .getId();
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  public AgendaView save(UUID workspace, UUID id, String title, OffsetDateTime startsAt) {
    AgendaEntity agenda = agendas.findByIdAndWorkspaceId(id, workspace).orElseThrow();
    agenda.setTitle(title);
    agenda.setStartsAt(startsAt);
    return view(agendas.save(agenda));
  }

  public AgendaView confirm(UUID workspace, UUID id) {
    AgendaEntity agenda = agendas.findByIdAndWorkspaceId(id, workspace).orElseThrow();
    agenda.setStatus(AgendaStatus.CONFIRMED);
    return view(agendas.save(agenda));
  }

  public void delete(UUID workspace, UUID id) {
    agendas.findByIdAndWorkspaceId(id, workspace).ifPresent(agendas::delete);
  }

  private AgendaView view(AgendaEntity agenda) {
    try {
      return new AgendaView(
          agenda.getId(),
          agenda.getTitle(),
          agenda.getStartsAt(),
          agenda.getStatus(),
          mapper.readValue(agenda.getSourceMessageIds(), new TypeReference<List<UUID>>() {}));
    } catch (java.io.IOException e) {
      throw new IllegalStateException("Invalid stored source ids", e);
    }
  }
}
