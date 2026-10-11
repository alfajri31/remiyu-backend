package id.remiyu.backend.service;

import id.remiyu.backend.dto.response.ChannelSummaryView;
import java.util.UUID;

public interface ChannelSummaryService {
  ChannelSummaryView generate(UUID user, UUID channel);
}
