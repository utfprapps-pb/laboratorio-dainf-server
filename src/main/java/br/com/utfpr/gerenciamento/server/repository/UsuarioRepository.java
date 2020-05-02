package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Usuario findByUsername(String username);

    List<Usuario> findByNomeLikeIgnoreCase(String query);

    @Query(value="SELECT U.* " +
            "FROM USUARIO U  " +
            "LEFT JOIN USUARIO_PERMISSOES UE " +
            " ON UE.USUARIO_ID = U.ID " +
            "LEFT JOIN PERMISSAO P " +
            " ON P.ID = UE.PERMISSOES_ID " +
            "WHERE ((UPPER(U.DOCUMENTO) LIKE :QUERY) " +
            " OR (UPPER(U.NOME) LIKE :QUERY) " +
            " OR (UPPER(U.USERNAME) LIKE :QUERY)) " , nativeQuery = true)
            //" AND ((P.NOME = 'ROLE_ALUNO') OR (P.NOME = 'ROLE_PROFESSOR'))", nativeQuery = true)
    List<Usuario> findUsuarioCompleteCustom(@Param("QUERY") String query);

    @Query(value="SELECT U.* " +
            "FROM USUARIO U  " +
            "LEFT JOIN USUARIO_PERMISSOES UE " +
            " ON UE.USUARIO_ID = U.ID " +
            "LEFT JOIN PERMISSAO P " +
            " ON P.ID = UE.PERMISSOES_ID " +
            "WHERE P.NOME = 'ROLE_ALUNO' " +
            " OR P.NOME = 'ROLE_PROFESSOR'", nativeQuery = true)
    List<Usuario> findAllCustom();
}
