package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.dto.EstadoResponseDto;
import br.com.utfpr.gerenciamento.server.model.Estado;
import br.com.utfpr.gerenciamento.server.repository.EstadoRepository;
import br.com.utfpr.gerenciamento.server.service.EstadoService;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstadoServiceImpl extends CrudServiceImpl<Estado, Long> implements EstadoService {

  private final EstadoRepository estadoRepository;

  private final ModelMapper modelMapper;

  public EstadoServiceImpl(EstadoRepository estadoRepository, ModelMapper modelMapper) {
    this.estadoRepository = estadoRepository;
    this.modelMapper = modelMapper;
  }

  @Override
  protected JpaRepository<Estado, Long> getRepository() {
    return this.estadoRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<EstadoResponseDto> estadoComplete(String query) {
    if ("".equalsIgnoreCase(query)) {
      return estadoRepository.findAll()
              .stream()
              .map(this::convertToDto)
              .toList();
    } else {
      return estadoRepository.findByNomeLikeIgnoreCase("%" + query + "%")
              .stream()
              .map(this::convertToDto)
              .toList();
    }
  }

  @Override
  public EstadoResponseDto convertToDto(Estado entity) {
    return modelMapper.map(entity, EstadoResponseDto.class);
  }
}
