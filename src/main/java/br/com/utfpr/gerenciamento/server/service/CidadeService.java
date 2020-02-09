package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.model.Cidade;

import java.util.List;

public interface CidadeService extends CrudService<Cidade, Long> {

    List<Cidade> cidadeComplete(String query);
}
