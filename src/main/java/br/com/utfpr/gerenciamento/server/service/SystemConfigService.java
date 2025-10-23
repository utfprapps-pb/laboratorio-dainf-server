package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.model.SystemConfig;
import java.util.Optional;

public interface SystemConfigService {
  /**
   * Recupera a configuração do sistema.
   *
   * @return Um Optional contendo a SystemConfig quando presente, caso contrário um Optional vazio.
   */
  Optional<SystemConfig> getConfig();

  /**
   * Persiste a configuração do sistema e retorna a instância salva.
   *
   * @param config a configuração do sistema a ser persistida
   * @return a instância de {@link SystemConfig} que foi persistida
   */
  SystemConfig saveConfig(SystemConfig config);

  /** Remove a configuração do sistema armazenada pelo serviço. */
  void deleteConfig();

  /**
   * Recupera o e-mail cadastrado para a situação "Nada Constou".
   *
   * @return Uma String contendo o e-mail para a situação "Nada Constou"
   */
  String getEmailNadaConsta();

  /**
   * Recupera a URL do logo para uso em templates e e-mails.
   *
   * @return a URL do logo, vinda de configuração ou do properties
   */
  String getLogoUrl();
}
