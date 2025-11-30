package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.dto.SolicitacaoListDto;
import br.com.utfpr.gerenciamento.server.dto.SolicitacaoResponseDto;
import br.com.utfpr.gerenciamento.server.model.Solicitacao;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SolicitacaoService extends CrudService<Solicitacao, Long, SolicitacaoResponseDto> {

  /**
   * Busca paginada para listagem com DTO simplificado.
   *
   * @param filter Filtro textual opcional
   * @param pageable Configuração de paginação
   * @return Página de DTOs simplificados
   */
  Page<SolicitacaoListDto> findAllPagedList(String filter, Pageable pageable);

  List<SolicitacaoResponseDto> findAllByUsername(String username);
}
