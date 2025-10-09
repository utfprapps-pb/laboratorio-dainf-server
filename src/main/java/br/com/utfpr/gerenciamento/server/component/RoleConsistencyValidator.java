package br.com.utfpr.gerenciamento.server.component;

import br.com.utfpr.gerenciamento.server.ennumeation.UserRole;
import br.com.utfpr.gerenciamento.server.model.Permissao;
import br.com.utfpr.gerenciamento.server.repository.PermissaoRepository;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Valida consistência entre enum UserRole e tabela permissao no banco de dados.
 *
 * <p>Executa na inicialização da aplicação para garantir que:
 *
 * <ul>
 *   <li>Todas as roles do enum existem no banco de dados
 *   <li>Roles no banco sem enum correspondente são identificadas (warning)
 * </ul>
 *
 * <p>Falha rápido (fail-fast) se roles do enum estiverem ausentes no banco, prevenindo problemas de
 * autorização em runtime.
 */
@Component
@RequiredArgsConstructor
public class RoleConsistencyValidator implements CommandLineRunner {

  private static final Logger LOGGER = Logger.getLogger(RoleConsistencyValidator.class.getName());

  private final PermissaoRepository permissaoRepository;

  @Override
  public void run(String... args) {
    LOGGER.info("Validando consistência entre UserRole enum e tabela permissao...");

    // 1. Verificar se todas as roles do enum existem no banco
    validateEnumRolesExistInDatabase();

    // 2. Identificar roles no banco sem enum correspondente
    identifyOrphanedRoles();

    LOGGER.info("Validação de roles concluída com sucesso.");
  }

  private void validateEnumRolesExistInDatabase() {
    for (UserRole role : UserRole.values()) {
      List<Permissao> permissoes = permissaoRepository.findAll();
      boolean existe = permissoes.stream().anyMatch(p -> p.getNome().equals(role.getAuthority()));

      if (!existe) {
        String mensagem =
            String.format(
                "ERRO CRÍTICO: Role do enum '%s' (authority='%s') não existe na tabela permissao. "
                    + "Adicione a role via migration ou remova do enum.",
                role.name(), role.getAuthority());
        LOGGER.severe(mensagem);
        throw new IllegalStateException(mensagem);
      }

      LOGGER.log(
          Level.INFO, "✓ Role {0} validada no banco de dados", new Object[] {role.getAuthority()});
    }
  }

  private void identifyOrphanedRoles() {
    List<String> enumAuthorities =
        Arrays.stream(UserRole.values()).map(UserRole::getAuthority).toList();

    List<String> rolesOrfaos =
        permissaoRepository.findAll().stream()
            .map(Permissao::getNome)
            .filter(nome -> !enumAuthorities.contains(nome))
            .toList();

    if (!rolesOrfaos.isEmpty()) {
      LOGGER.log(
          Level.WARNING,
          "Roles no banco sem enum correspondente: {0}. "
              + "Considere adicionar ao enum UserRole se forem roles ativas.",
          new Object[] {rolesOrfaos});
    }
  }
}
