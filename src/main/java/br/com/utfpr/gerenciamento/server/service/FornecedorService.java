package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.model.Fornecedor;

import java.util.List;

public interface FornecedorService extends CrudService<Fornecedor, Long> {

    List<Fornecedor> completeFornecedor(String query);
}
