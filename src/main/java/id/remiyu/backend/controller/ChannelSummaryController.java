package id.remiyu.backend.controller;

import id.remiyu.backend.dto.response.ChannelSummaryView;
import id.remiyu.backend.dto.response.UserView;
import id.remiyu.backend.service.ChannelSummaryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/channels/{channel}/summary")
public class ChannelSummaryController {
  private final ChannelSummaryService service;

  @PostMapping
  public ChannelSummaryView generate(
      @AuthenticationPrincipal UserView user, @PathVariable UUID channel) {
    return service.generate(user.id(), channel);
  }
}
