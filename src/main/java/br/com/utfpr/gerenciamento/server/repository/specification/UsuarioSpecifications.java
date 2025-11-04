package br.com.utfpr.gerenciamento.server.repository.specification;

import br.com.utfpr.gerenciamento.server.enumeration.UserRole;
import br.com.utfpr.gerenciamento.server.model.Permissao;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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

  public static final String USERNAME_TEXT = "username";
  public static final String DOCUMENTO_TEXT = "documento";

  private UsuarioSpecifications() {
    // Utility class - construtor privado
  }

  /**
   * Obt Obtain existing permissoes JOIN or creates a new one.
   *
   * <p>Prevents duplicate JOINs on the same table by reusing existing joins. This optimization is
   * critical when combining multiple specifications that all need to filter by user roles.
   *
   * @param root the root entity
   * @return existing or new JOIN on permissoes
   */
  @SuppressWarnings("unchecked")
  private static Join<Usuario, Permissao> getOrCreatePermissoesJoin(Root<Usuario> root) {
    // Reutiliza apenas JOIN em "permissoes" com mesmo JoinType (INNER)
    for (Join<?, ?> join : root.getJoins()) {
      if ("permissoes".equals(join.getAttribute().getName())
          && join.getJoinType() == JoinType.INNER) {
        return (Join<Usuario, Permissao>) join;
      }
    }
    // Cria INNER JOIN se não existir equivalente
    return root.join("permissoes", JoinType.INNER);
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

      // Reusa JOIN existente ou cria novo
      Join<Usuario, Permissao> permissoesJoin = getOrCreatePermissoesJoin(root);

      // WHERE permissao.nome IN ('ROLE_PROFESSOR', 'ROLE_ALUNO', ...)
      return permissoesJoin.get("nome").in(roleNames);
    };
  }

  /**
   * Filtra usuários por roles e opcionalmente por texto (nome/username/documento).
   *
   * <p>Este método unifica a filtragem por roles com busca textual opcional, criando apenas UM JOIN
   * na tabela permissoes. Se o texto de busca for nulo ou vazio, retorna todos os usuários com as
   * roles especificadas.
   *
   * <p>Substitui o padrão anti-pattern de combinar hasAnyRole() + searchByTextAndRoles() que criava
   * JOINs duplicados.
   *
   * <p>Exemplo: searchByTextWithRoles(null, UserRole.PROFESSOR) retorna todos professores
   * searchByTextWithRoles("joão", UserRole.PROFESSOR, UserRole.ALUNO) retorna professores e alunos
   * cujo nome/username/documento contenha "joão"
   *
   * @param searchText texto para buscar (case-insensitive, busca parcial). Pode ser null/blank.
   * @param roles varargs de UserRole para filtrar (obrigatório)
   * @return Specification combinando filtros de roles e texto (opcional)
   */
  public static Specification<Usuario> searchByTextWithRoles(String searchText, UserRole... roles) {
    if (roles == null || roles.length == 0) {
      throw new IllegalArgumentException("Roles não podem ser nulas ou vazias");
    }

    return (root, query, cb) -> {
      // Reusa JOIN existente ou cria novo (apenas 1 JOIN)
      Join<Usuario, Permissao> permissoesJoin = getOrCreatePermissoesJoin(root);

      // Filtro de roles (obrigatório)
      Set<String> roleNames =
          Arrays.stream(roles).map(UserRole::getAuthority).collect(Collectors.toSet());
      Predicate rolesPredicate = permissoesJoin.get("nome").in(roleNames);

      // Filtro textual (opcional)
      if (searchText == null || searchText.isBlank()) {
        // Sem filtro textual: retorna todos usuários com as roles
        return rolesPredicate;
      }

      // Com filtro textual: combina ambos os filtros
      String upperText = "%" + searchText.toUpperCase() + "%";
      Predicate textPredicate =
          cb.or(
              cb.like(cb.upper(root.get("nome")), upperText),
              cb.like(cb.upper(root.get(USERNAME_TEXT)), upperText),
              cb.like(cb.upper(root.get(DOCUMENTO_TEXT)), upperText));

      // WHERE (nome LIKE OR username LIKE OR documento LIKE) AND (permissao.nome IN ...)
      return cb.and(textPredicate, rolesPredicate);
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
              cb.like(cb.upper(root.get(USERNAME_TEXT)), upperText),
              cb.like(cb.upper(root.get(DOCUMENTO_TEXT)), upperText));

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
          cb.like(cb.upper(root.get(USERNAME_TEXT)), upperText),
          cb.like(cb.upper(root.get(DOCUMENTO_TEXT)), upperText));
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
      if (query != null) {
        query.distinct(true);
      }

      return cb.conjunction(); // WHERE 1=1 (no-op predicate)
    };
  }
}
