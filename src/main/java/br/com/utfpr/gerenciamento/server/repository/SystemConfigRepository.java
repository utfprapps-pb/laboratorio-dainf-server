package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.SystemConfig;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
  /**
   * Busca a primeira configuração do sistema marcada como ativa.
   *
   * @return um {@link SystemConfig} opcional contendo a primeira configuração com `isActive =
   *     true`, vazio se nenhuma for encontrada
   */
  Optional<SystemConfig> findFirstByIsActiveTrue();

  /**
   * Busca uma configuração do sistema pela sua chave.
   *
   * @param key a chave da configuração a ser buscada
   * @return um {@link SystemConfig} opcional contendo a configuração correspondente à chave, vazio
   *     se nenhuma for encontrada
   */
  Optional<SystemConfig> findByKey(String key);
}
