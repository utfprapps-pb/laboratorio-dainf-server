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

  /**
   * Cria uma instância de SystemConfigServiceImpl usando o repositório fornecido.
   *
   * @param repository repositório para acesso e persistência de entidades SystemConfig
   */
  public SystemConfigServiceImpl(SystemConfigRepository repository) {
    this.repository = repository;
  }

  /**
   * Obtém a configuração do sistema marcada como ativa.
   *
   * @return Optional contendo a SystemConfig ativa, vazio se nenhuma configuração ativa existir.
   */
  @Override
  public Optional<SystemConfig> getConfig() {
    return repository.findFirstByIsActiveTrue();
  }

  /**
   * Persiste a configuração de sistema ativa, criando uma configuração base se nenhuma existir.
   *
   * <p>Atualiza o campo `nadaConstaEmail` da configuração ativa com o valor fornecido e salva a
   * entidade.
   *
   * @param config configuração com os valores a serem aplicados na configuração ativa (usa
   *     `nadaConstaEmail`)
   * @return a entidade SystemConfig salva representando a configuração ativa após a atualização
   */
  @Override
  @Transactional
  public SystemConfig saveConfig(SystemConfig config) {
    SystemConfig existingConfig =
        getConfig()
            .orElseGet(
                () -> {
                  SystemConfig sc = new SystemConfig();
                  sc.setId(1L);
                  return sc;
                });
    existingConfig.setNadaConstaEmail(config.getNadaConstaEmail());
    return repository.save(existingConfig);
  }

  /**
   * Remove a configuração de sistema atualmente ativa, se existir.
   *
   * <p>Se não houver configuração ativa, o método não realiza nenhuma ação.
   */
  @Override
  @Transactional
  public void deleteConfig() {
    Optional<SystemConfig> configOpt = getConfig();
    configOpt.ifPresent(repository::delete);
  }
}
