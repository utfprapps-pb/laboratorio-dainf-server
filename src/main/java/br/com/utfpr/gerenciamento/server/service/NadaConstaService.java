package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.dto.NadaConstaResponseDto;
import br.com.utfpr.gerenciamento.server.model.NadaConsta;
import java.util.List;

public interface NadaConstaService extends CrudService<NadaConsta, Long> {
  List<NadaConstaResponseDto> findAllByUsername(String username);

  NadaConstaResponseDto solicitarNadaConsta(String documento);

  NadaConstaResponseDto convertToDto(NadaConsta entity);
}
