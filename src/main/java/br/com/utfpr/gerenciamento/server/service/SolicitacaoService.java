package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.model.Solicitacao;
import java.util.List;

public interface SolicitacaoService extends CrudService<Solicitacao, Long> {

  List<Solicitacao> findAllByUsername(String username);
}
