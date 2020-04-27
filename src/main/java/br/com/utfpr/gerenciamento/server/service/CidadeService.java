package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.model.Cidade;
import br.com.utfpr.gerenciamento.server.model.Estado;

import java.util.List;

public interface CidadeService extends CrudService<Cidade, Long> {

    List<Cidade> cidadeComplete(String query);

    List<Cidade> completeByEstado(String query, Estado estado);
}
