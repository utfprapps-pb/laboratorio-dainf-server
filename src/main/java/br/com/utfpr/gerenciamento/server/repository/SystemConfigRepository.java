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
 * @return um {@link SystemConfig} opcional contendo a primeira configuração com `isActive = true`, vazio se nenhuma for encontrada
 */
Optional<SystemConfig> findFirstByIsActiveTrue();
}