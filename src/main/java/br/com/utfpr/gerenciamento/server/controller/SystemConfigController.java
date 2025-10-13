package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.SystemConfig;
import br.com.utfpr.gerenciamento.server.service.SystemConfigService;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("config")
@Validated
public class SystemConfigController {
  private final SystemConfigService service;

  public SystemConfigController(SystemConfigService service) {
    this.service = service;
  }

  @GetMapping
  @PreAuthorize("hasAuthority('ADMINISTRADOR')")
  public ResponseEntity<SystemConfig> getConfig() {
    Optional<SystemConfig> configOpt = service.getConfig();
    return configOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping
  @PreAuthorize("hasAuthority('ADMINISTRADOR')")
  public ResponseEntity<SystemConfig> saveConfig(@Valid @RequestBody SystemConfig config) {
    SystemConfig savedConfig = service.saveConfig(config);
    return ResponseEntity.ok(savedConfig);
  }
}
