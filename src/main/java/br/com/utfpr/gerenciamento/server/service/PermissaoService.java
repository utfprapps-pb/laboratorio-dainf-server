package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.model.Permissao;

public interface PermissaoService extends CrudService<Permissao, Long> {

  Permissao findByNome(String nome);
}
