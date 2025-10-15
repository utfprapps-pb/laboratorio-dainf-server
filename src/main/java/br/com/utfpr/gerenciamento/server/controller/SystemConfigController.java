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

  /**
   * Cria uma instância de SystemConfigController com o serviço de configuração do sistema.
   *
   * @param service serviço responsável por operações de leitura e persistência de SystemConfig
   */
  public SystemConfigController(SystemConfigService service) {
    this.service = service;
  }

  /**
   * Obtém a configuração do sistema.
   *
   * @return ResponseEntity contendo a configuração com status 200 quando presente, ou resposta 404
   *     Not Found quando não houver configuração.
   */
  @GetMapping
  @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
  public ResponseEntity<SystemConfig> getConfig() {
    Optional<SystemConfig> configOpt = service.getConfig();
    return configOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  /**
   * Salva a configuração do sistema após validar o e-mail de contato.
   *
   * @param config objeto de configuração do sistema; o campo `nadaConstaEmail` deve ser não nulo e
   *     terminar com `@utfpr.edu.br`
   * @return `ResponseEntity` com o `SystemConfig` salvo no corpo e status 200; se o e-mail for nulo
   *     ou não terminar com `@utfpr.edu.br`, retorna 400 (Bad Request) sem corpo
   */
  @PostMapping
  @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
  public ResponseEntity<SystemConfig> saveConfig(@Valid @RequestBody SystemConfig config) {
    SystemConfig savedConfig = service.saveConfig(config);
    return ResponseEntity.ok(savedConfig);
  }
}
