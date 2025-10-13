package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.model.SystemConfig;
import java.util.Optional;

public interface SystemConfigService {
  Optional<SystemConfig> getConfig();

  SystemConfig saveConfig(SystemConfig config);
}
