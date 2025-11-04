package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.dto.FornecedorResponseDto;
import br.com.utfpr.gerenciamento.server.model.Fornecedor;
import java.util.List;

public interface FornecedorService extends CrudService<Fornecedor, Long, FornecedorResponseDto> {

  List<Fornecedor> completeFornecedor(String query);

  FornecedorResponseDto convertToDto(Fornecedor entity);
}
