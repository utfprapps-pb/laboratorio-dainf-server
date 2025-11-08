package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.dto.PaisResponseDto;
import br.com.utfpr.gerenciamento.server.model.Pais;
import java.util.List;

public interface PaisService extends CrudService<Pais, Long, PaisResponseDto> {

  List<PaisResponseDto> paisComplete(String query);
}
