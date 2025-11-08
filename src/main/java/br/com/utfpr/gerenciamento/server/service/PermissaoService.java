package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.dto.PermissaoResponseDTO;
import br.com.utfpr.gerenciamento.server.model.Permissao;

public interface PermissaoService extends CrudService<Permissao, Long, PermissaoResponseDTO> {

  Permissao findByNome(String nome);
}
