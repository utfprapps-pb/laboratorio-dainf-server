package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UsuarioRepository
    extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

  Usuario findByCodigoVerificacao(String codigoVerificacao);

  /**
   * Busca usuario por username SEM carregar permissoes (LAZY). Use para operações que não precisam
   * de permissões.
   */
  Usuario findByUsername(String username);

  /**
   * Busca usuario por username COM permissoes carregadas (para autenticação). Use nos fluxos de
   * autenticação/autorização onde UserDetails precisa das permissões.
   */
  @EntityGraph(attributePaths = {"permissoes"})
  Usuario findWithPermissoesByUsername(String username);

  /**
   * Busca usuario por username ou email SEM carregar permissoes (LAZY). Use para operações que não
   * precisam de permissões.
   */
  Usuario findByUsernameOrEmail(String username, String email);

  /**
   * Busca usuario por username ou email COM permissoes carregadas (para autenticação). Use no fluxo
   * de autenticação onde UserDetails.getAuthorities() precisa das permissões.
   */
  @EntityGraph(attributePaths = {"permissoes"})
  Usuario findWithPermissoesByUsernameOrEmail(String username, String email);

  Usuario findByEmail(String email);

  java.util.Optional<Usuario> findByDocumento(String documento);

  /**
   * Busca paginada de usuários com permissões carregadas usando Specification.
   *
   * <p>Substitui queries nativas com IDs hardcoded por filtros type-safe baseados em roles. Use com
   * UsuarioSpecifications para filtrar por roles e/ou texto.
   *
   * <p>Exemplo: {@code repository.findAll(UsuarioSpecifications.hasAnyRole(UserRole.PROFESSOR,
   * UserRole.ALUNO).and(UsuarioSpecifications.distinctResults()), pageable)}
   *
   * @param spec Specification para filtros (roles, busca textual, etc.)
   * @param pageable paginação e ordenação
   * @return Page de Usuario com permissões carregadas via @EntityGraph
   */
  @EntityGraph(attributePaths = {"permissoes"})
  Page<Usuario> findAll(Specification<Usuario> spec, Pageable pageable);
}
