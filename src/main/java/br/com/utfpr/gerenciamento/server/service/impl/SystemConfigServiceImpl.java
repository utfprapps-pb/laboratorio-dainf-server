package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.SystemConfig;
import br.com.utfpr.gerenciamento.server.repository.SystemConfigRepository;
import br.com.utfpr.gerenciamento.server.service.SystemConfigService;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigServiceImpl implements SystemConfigService {
  private final SystemConfigRepository repository;

  public SystemConfigServiceImpl(SystemConfigRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<SystemConfig> getConfig() {
    return repository.findAll().stream().findFirst();
  }

  @Override
  @Transactional
  public SystemConfig saveConfig(SystemConfig config) {
    SystemConfig existingConfig = getConfig().orElse(new SystemConfig());
    existingConfig.setNadaConstaEmail(config.getNadaConstaEmail());
    return repository.save(existingConfig);
  }

  @Override
  @Transactional
  public void deleteConfig() {
    Optional<SystemConfig> configOpt = getConfig();
    configOpt.ifPresent(repository::delete);
  }
}
