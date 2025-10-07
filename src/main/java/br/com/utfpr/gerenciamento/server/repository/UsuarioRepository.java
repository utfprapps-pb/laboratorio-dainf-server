package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Usuario;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

  List<Usuario> findByNomeLikeIgnoreCase(String query);

  @Query(
      value =
          "SELECT DISTINCT U.* "
              + "FROM USUARIO U  "
              + "LEFT JOIN USUARIO_PERMISSOES UE "
              + " ON UE.USUARIO_ID = U.ID "
              + "LEFT JOIN PERMISSAO P "
              + " ON P.ID = UE.PERMISSOES_ID "
              + "WHERE ((UPPER(U.DOCUMENTO) LIKE :QUERY) "
              + " OR (UPPER(U.NOME) LIKE :QUERY) "
              + " OR (UPPER(U.USERNAME) LIKE :QUERY)) "
              + " AND P.ID IN (3, 4) ",
      nativeQuery = true)
  List<Usuario> findUsuarioCompleteCustom(@Param("QUERY") String query);

  @Query(
      value =
          "SELECT DISTINCT U.* "
              + "FROM USUARIO U  "
              + "LEFT JOIN USUARIO_PERMISSOES UE "
              + " ON UE.USUARIO_ID = U.ID "
              + "LEFT JOIN PERMISSAO P "
              + " ON P.ID = UE.PERMISSOES_ID "
              + "WHERE P.ID IN (3, 4) ",
      nativeQuery = true)
  List<Usuario> findAllCustom();

  @Query(
      value =
          "SELECT DISTINCT U.* "
              + "FROM USUARIO U  "
              + "LEFT JOIN USUARIO_PERMISSOES UE "
              + " ON UE.USUARIO_ID = U.ID "
              + "LEFT JOIN PERMISSAO P "
              + " ON P.ID = UE.PERMISSOES_ID "
              + "WHERE P.ID IN (1, 2) ",
      nativeQuery = true)
  List<Usuario> findAllCustomLab();

  @Query(
      value =
          "SELECT DISTINCT U.* "
              + "FROM USUARIO U  "
              + "LEFT JOIN USUARIO_PERMISSOES UE "
              + " ON UE.USUARIO_ID = U.ID "
              + "LEFT JOIN PERMISSAO P "
              + " ON P.ID = UE.PERMISSOES_ID "
              + "WHERE ((UPPER(U.DOCUMENTO) LIKE :QUERY) "
              + " OR (UPPER(U.NOME) LIKE :QUERY) "
              + " OR (UPPER(U.USERNAME) LIKE :QUERY)) "
              + " AND P.ID IN (1, 2) ",
      nativeQuery = true)
  List<Usuario> findUsuarioCompleteCustomLab(@Param("QUERY") String query);
}
