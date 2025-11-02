package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.dto.CidadeResponseDto;
import br.com.utfpr.gerenciamento.server.model.Cidade;
import br.com.utfpr.gerenciamento.server.model.Estado;
import java.util.List;

public interface CidadeService extends CrudService<Cidade, Long,CidadeResponseDto> {

  List<CidadeResponseDto> cidadeComplete(String query);

  List<CidadeResponseDto> completeByEstado(String query, Estado estado);

  CidadeResponseDto convertToDto(Cidade entity);
}
