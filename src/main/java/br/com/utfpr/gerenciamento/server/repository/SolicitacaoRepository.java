package br.com.utfpr.gerenciamento.server.repository;

import br.com.utfpr.gerenciamento.server.model.Solicitacao;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.repository.projection.SolicitacaoListProjection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SolicitacaoRepository
    extends JpaRepository<Solicitacao, Long>, JpaSpecificationExecutor<Solicitacao> {

  List<Solicitacao> findAllByUsuario(Usuario usuario);

  /**
   * Busca paginada de solicitações para listagem com campos otimizados.
   *
   * @param pageable paginação e ordenação
   * @return Page de projeções com apenas campos necessários para tabela
   */
  @Query(
      """
      SELECT s.id as id,
             s.descricao as descricao,
             s.dataSolicitacao as dataSolicitacao,
             u.nome as usuarioNome
      FROM Solicitacao s
      LEFT JOIN s.usuario u
      """)
  Page<SolicitacaoListProjection> findAllProjected(Pageable pageable);

  /**
   * Busca paginada de solicitações com filtro de texto.
   *
   * @param filter texto para filtrar por id, descrição ou usuário
   * @param pageable paginação e ordenação
   * @return Page de projeções filtradas
   */
  @Query(
      """
      SELECT s.id as id,
             s.descricao as descricao,
             s.dataSolicitacao as dataSolicitacao,
             u.nome as usuarioNome
      FROM Solicitacao s
      LEFT JOIN s.usuario u
      WHERE CAST(s.id AS string) LIKE CONCAT('%', :filter, '%')
         OR LOWER(s.descricao) LIKE LOWER(CONCAT('%', :filter, '%'))
         OR LOWER(u.nome) LIKE LOWER(CONCAT('%', :filter, '%'))
      """)
  Page<SolicitacaoListProjection> findAllProjectedWithFilter(
      @Param("filter") String filter, Pageable pageable);
}
