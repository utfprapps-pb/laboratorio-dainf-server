package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.dto.PaisResponseDto;
import br.com.utfpr.gerenciamento.server.model.Pais;
import br.com.utfpr.gerenciamento.server.repository.PaisRepository;
import br.com.utfpr.gerenciamento.server.service.PaisService;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaisServiceImpl extends CrudServiceImpl<Pais, Long> implements PaisService {

  private final PaisRepository paisRepository;

  private final ModelMapper modelMapper;

  public PaisServiceImpl(PaisRepository paisRepository, ModelMapper modelMapper) {
    this.paisRepository = paisRepository;
    this.modelMapper = modelMapper;
  }

  @Override
  protected JpaRepository<Pais, Long> getRepository() {
    return this.paisRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<PaisResponseDto> paisComplete(String query) {
    if ("".equalsIgnoreCase(query)) {
      return this.paisRepository.findAll().stream().map(this::convertToDto).toList();
    } else {
      return this.paisRepository.findByNomeLikeIgnoreCase("%" + query + "%").stream()
          .map(this::convertToDto)
          .toList();
    }
  }

  @Override
  public PaisResponseDto convertToDto(Pais entity) {
    return modelMapper.map(entity, PaisResponseDto.class);
  }
}
