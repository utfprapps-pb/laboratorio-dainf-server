package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.dto.CidadeResponseDto;
import br.com.utfpr.gerenciamento.server.model.Cidade;
import br.com.utfpr.gerenciamento.server.model.Estado;
import br.com.utfpr.gerenciamento.server.repository.CidadeRepository;
import br.com.utfpr.gerenciamento.server.service.CidadeService;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CidadeServiceImpl extends CrudServiceImpl<Cidade, Long> implements CidadeService {

  private final CidadeRepository cidadeRepository;

  private final ModelMapper modelMapper;

  public CidadeServiceImpl(CidadeRepository cidadeRepository, ModelMapper modelMapper) {
    this.cidadeRepository = cidadeRepository;
    this.modelMapper = modelMapper;
  }

  @Override
  protected JpaRepository<Cidade, Long> getRepository() {
    return cidadeRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<CidadeResponseDto> cidadeComplete(String query) {
    if (query == null || query.isBlank()) {
      return this.cidadeRepository.findAll()
              .stream()
              .map(this::convertToDto)
              .toList();
    } else {
      return this.cidadeRepository.findByNomeLikeIgnoreCase("%" + query + "%")
              .stream()
              .map(this::convertToDto)
              .toList();
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<CidadeResponseDto> completeByEstado(String query, Estado estado) {
    if (estado == null) return List.of();

    if (query == null || query.isBlank()) {
      return this.cidadeRepository.findAllByEstado(estado)
              .stream()
              .map(this::convertToDto)
              .toList();
    } else {
      return this.cidadeRepository.findByNomeLikeIgnoreCaseAndEstado("%" + query + "%", estado)
              .stream()
              .map(this::convertToDto)
              .toList();
    }
  }

  @Override
  public CidadeResponseDto convertToDto(Cidade entity) {
    return modelMapper.map(entity, CidadeResponseDto.class);
  }
}
