package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.dto.GrupoResponseDto;
import br.com.utfpr.gerenciamento.server.model.Grupo;
import java.util.List;

public interface GrupoService extends CrudService<Grupo, Long> {

  List<GrupoResponseDto> completeGrupo(String query);

  GrupoResponseDto convertToDto(Grupo entity);
}
