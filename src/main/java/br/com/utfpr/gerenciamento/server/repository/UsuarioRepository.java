package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

  Page<Usuario> findByNomeLikeIgnoreCase(String query, Pageable pageable);

  @Query(
      value =
          """
          SELECT DISTINCT U.*
          FROM USUARIO U
          LEFT JOIN USUARIO_PERMISSOES UE
              ON UE.USUARIO_ID = U.ID
          LEFT JOIN PERMISSAO P
              ON P.ID = UE.PERMISSOES_ID
          WHERE ((UPPER(U.DOCUMENTO) LIKE :QUERY)
              OR (UPPER(U.NOME) LIKE :QUERY)
              OR (UPPER(U.USERNAME) LIKE :QUERY))
              AND P.ID IN (3, 4)
          """,
      countQuery =
          """
          SELECT COUNT(DISTINCT U.ID)
          FROM USUARIO U
          LEFT JOIN USUARIO_PERMISSOES UE
              ON UE.USUARIO_ID = U.ID
          LEFT JOIN PERMISSAO P
              ON P.ID = UE.PERMISSOES_ID
          WHERE ((UPPER(U.DOCUMENTO) LIKE :QUERY)
              OR (UPPER(U.NOME) LIKE :QUERY)
              OR (UPPER(U.USERNAME) LIKE :QUERY))
              AND P.ID IN (3, 4)
          """,
      nativeQuery = true)
  Page<Usuario> findUsuarioCompleteCustom(@Param("QUERY") String query, Pageable pageable);

  @Query(
      value =
          """
          SELECT DISTINCT U.*
          FROM USUARIO U
          LEFT JOIN USUARIO_PERMISSOES UE
              ON UE.USUARIO_ID = U.ID
          LEFT JOIN PERMISSAO P
              ON P.ID = UE.PERMISSOES_ID
          WHERE P.ID IN (3, 4)
          """,
      countQuery =
          """
          SELECT COUNT(DISTINCT U.ID)
          FROM USUARIO U
          LEFT JOIN USUARIO_PERMISSOES UE
              ON UE.USUARIO_ID = U.ID
          LEFT JOIN PERMISSAO P
              ON P.ID = UE.PERMISSOES_ID
          WHERE P.ID IN (3, 4)
          """,
      nativeQuery = true)
  Page<Usuario> findAllCustom(Pageable pageable);

  @Query(
      value =
          """
          SELECT DISTINCT U.*
          FROM USUARIO U
          LEFT JOIN USUARIO_PERMISSOES UE
              ON UE.USUARIO_ID = U.ID
          LEFT JOIN PERMISSAO P
              ON P.ID = UE.PERMISSOES_ID
          WHERE P.ID IN (1, 2)
          """,
      countQuery =
          """
          SELECT COUNT(DISTINCT U.ID)
          FROM USUARIO U
          LEFT JOIN USUARIO_PERMISSOES UE
              ON UE.USUARIO_ID = U.ID
          LEFT JOIN PERMISSAO P
              ON P.ID = UE.PERMISSOES_ID
          WHERE P.ID IN (1, 2)
          """,
      nativeQuery = true)
  Page<Usuario> findAllCustomLab(Pageable pageable);

  @Query(
      value =
          """
          SELECT DISTINCT U.*
          FROM USUARIO U
          LEFT JOIN USUARIO_PERMISSOES UE
              ON UE.USUARIO_ID = U.ID
          LEFT JOIN PERMISSAO P
              ON P.ID = UE.PERMISSOES_ID
          WHERE ((UPPER(U.DOCUMENTO) LIKE :QUERY)
              OR (UPPER(U.NOME) LIKE :QUERY)
              OR (UPPER(U.USERNAME) LIKE :QUERY))
              AND P.ID IN (1, 2)
          """,
      countQuery =
          """
          SELECT COUNT(DISTINCT U.ID)
          FROM USUARIO U
          LEFT JOIN USUARIO_PERMISSOES UE
              ON UE.USUARIO_ID = U.ID
          LEFT JOIN PERMISSAO P
              ON P.ID = UE.PERMISSOES_ID
          WHERE ((UPPER(U.DOCUMENTO) LIKE :QUERY)
              OR (UPPER(U.NOME) LIKE :QUERY)
              OR (UPPER(U.USERNAME) LIKE :QUERY))
              AND P.ID IN (1, 2)
          """,
      nativeQuery = true)
  Page<Usuario> findUsuarioCompleteCustomLab(@Param("QUERY") String query, Pageable pageable);
}
