package id.remiyu.backend.controller;

import id.remiyu.backend.dto.response.DashboardView;
import id.remiyu.backend.dto.response.UserView;
import id.remiyu.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {
  private final DashboardService service;

  @GetMapping
  public DashboardView get(@AuthenticationPrincipal UserView user) {
    return service.get(user.id());
  }
}
