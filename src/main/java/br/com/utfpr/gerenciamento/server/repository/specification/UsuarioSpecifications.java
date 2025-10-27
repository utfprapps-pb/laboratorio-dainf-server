package br.com.utfpr.gerenciamento.server.repository.specification;

import br.com.utfpr.gerenciamento.server.enumeration.UserRole;
import br.com.utfpr.gerenciamento.server.model.Permissao;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications para filtros type-safe de Usuario baseados em roles e busca textual.
 *
 * <p>Substitui queries nativas com IDs hardcoded por filtros baseados em nomes de roles do enum
 * UserRole, eliminando acoplamento com estado do banco de dados e riscos de bypass de autorização.
 */
public class UsuarioSpecifications {

  private UsuarioSpecifications() {
    // Utility class - construtor privado
  }

  /**
   * Filtra usuários que possuem qualquer uma das roles fornecidas.
   *
   * <p>Exemplo: hasAnyRole(UserRole.PROFESSOR, UserRole.ALUNO) retorna usuários com role
   * ROLE_PROFESSOR OU ROLE_ALUNO.
   *
   * @param roles varargs de UserRole para filtrar
   * @return Specification que filtra por roles via JOIN em permissoes
   */
  public static Specification<Usuario> hasAnyRole(UserRole... roles) {
    if (roles == null || roles.length == 0) {
      throw new IllegalArgumentException("Roles não podem ser nulas ou vazias");
    }

    return (root, query, cb) -> {
      // Converte UserRole[] → Set<String> com valores "ROLE_XXX" do banco
      Set<String> roleNames =
          Arrays.stream(roles).map(UserRole::getAuthority).collect(Collectors.toSet());

      // JOIN INNER em Usuario.permissoes
      Join<Usuario, Permissao> permissoesJoin = root.join("permissoes", JoinType.INNER);

      // WHERE permissao.nome IN ('ROLE_PROFESSOR', 'ROLE_ALUNO', ...)
      return permissoesJoin.get("nome").in(roleNames);
    };
  }

  /**
   * Filtra usuários por texto (nome/username/documento) E roles específicas.
   *
   * <p>Combina busca textual com filtro de roles. Exemplo: searchByTextAndRoles("admin",
   * UserRole.ADMINISTRADOR) retorna apenas administradores cujo nome/username/documento contenha
   * "admin".
   *
   * @param searchText texto para buscar (case-insensitive, busca parcial)
   * @param roles varargs de UserRole para filtrar
   * @return Specification combinando filtros textuais e de roles
   */
  public static Specification<Usuario> searchByTextAndRoles(String searchText, UserRole... roles) {
    if (searchText == null || searchText.isBlank()) {
      throw new IllegalArgumentException("Texto de busca não pode ser nulo ou vazio");
    }
    if (roles == null || roles.length == 0) {
      throw new IllegalArgumentException("Roles não podem ser nulas ou vazias");
    }

    return (root, query, cb) -> {
      // Predicado de roles (JOIN em permissoes)
      Predicate rolesPredicate = hasAnyRole(roles).toPredicate(root, query, cb);

      // Predicado de busca textual (UPPER LIKE para case-insensitive)
      String upperText = "%" + searchText.toUpperCase() + "%";
      Predicate textPredicate =
          cb.or(
              cb.like(cb.upper(root.get("nome")), upperText),
              cb.like(cb.upper(root.get("username")), upperText),
              cb.like(cb.upper(root.get("documento")), upperText));

      // WHERE (nome LIKE OR username LIKE OR documento LIKE) AND (permissao.nome IN ...)
      return cb.and(textPredicate, rolesPredicate);
    };
  }

  /**
   * Filtra usuários por texto (nome/username/documento) sem filtro de roles.
   *
   * <p>Busca textual simples em todos os campos de identificação do usuário. Útil para buscas
   * genéricas sem restrição de permissão. Exemplo: searchByText("joão") retorna todos os usuários
   * cujo nome, username ou documento contenha "joão".
   *
   * @param searchText texto para buscar (case-insensitive, busca parcial)
   * @return Specification com filtro textual
   */
  public static Specification<Usuario> searchByText(String searchText) {
    if (searchText == null || searchText.isBlank()) {
      throw new IllegalArgumentException("Texto de busca não pode ser nulo ou vazio");
    }

    return (root, query, cb) -> {
      String upperText = "%" + searchText.toUpperCase() + "%";
      return cb.or(
          cb.like(cb.upper(root.get("nome")), upperText),
          cb.like(cb.upper(root.get("username")), upperText),
          cb.like(cb.upper(root.get("documento")), upperText));
    };
  }

  /**
   * Aplica DISTINCT para evitar duplicatas em consultas com JOIN ManyToMany.
   *
   * <p>Necessário quando um usuário tem múltiplas permissões - sem DISTINCT, o JOIN retornaria
   * linhas duplicadas (uma por permissão).
   *
   * @return Specification que aplica DISTINCT na query
   */
  public static Specification<Usuario> distinctResults() {
    return (root, query, cb) -> {
      query.distinct(true);
      return cb.conjunction(); // WHERE 1=1 (no-op predicate)
    };
  }
}
