package id.remiyu.backend.controller;

import id.remiyu.backend.dto.request.CreateChannelRequest;
import id.remiyu.backend.dto.response.ChannelView;
import id.remiyu.backend.dto.response.InvitationResponse;
import id.remiyu.backend.dto.response.MemberView;
import id.remiyu.backend.dto.response.UserView;
import id.remiyu.backend.service.ChannelService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/channels")
@RequiredArgsConstructor
public class ChannelController {
  private final ChannelService service;

  @GetMapping
  public List<ChannelView> list(@AuthenticationPrincipal UserView user) {
    return service.list(user.id());
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ChannelView create(
      @AuthenticationPrincipal UserView user, @Valid @RequestBody CreateChannelRequest body) {
    return service.create(user.id(), body.name(), body.platform(), body.account());
  }

  @GetMapping("/{id}")
  public ChannelView get(@AuthenticationPrincipal UserView user, @PathVariable UUID id) {
    return service.get(id, user.id());
  }

  @GetMapping("/{id}/members")
  public List<MemberView> members(@AuthenticationPrincipal UserView user, @PathVariable UUID id) {
    return service.members(id, user.id());
  }

  @PostMapping("/{id}/invitation")
  public InvitationResponse invite(@AuthenticationPrincipal UserView user, @PathVariable UUID id) {
    return service.invite(id, user.id());
  }

  @PostMapping("/join/{token}")
  public ChannelView join(@AuthenticationPrincipal UserView user, @PathVariable UUID token) {
    return service.join(token, user.id());
  }
}
