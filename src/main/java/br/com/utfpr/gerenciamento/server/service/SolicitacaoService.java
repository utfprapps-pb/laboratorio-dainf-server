package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.dto.SolicitacaoResponseDto;
import br.com.utfpr.gerenciamento.server.model.Solicitacao;
import java.util.List;

public interface SolicitacaoService extends CrudService<Solicitacao, Long, SolicitacaoResponseDto> {

  List<SolicitacaoResponseDto> findAllByUsername(String username);
}
