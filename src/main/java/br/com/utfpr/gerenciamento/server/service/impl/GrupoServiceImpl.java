package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.dto.GrupoResponseDto;
import br.com.utfpr.gerenciamento.server.model.Grupo;
import br.com.utfpr.gerenciamento.server.repository.GrupoRepository;
import br.com.utfpr.gerenciamento.server.service.GrupoService;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GrupoServiceImpl extends CrudServiceImpl<Grupo, Long> implements GrupoService {

  private final GrupoRepository grupoRepository;

  private final ModelMapper modelMapper;

  public GrupoServiceImpl(GrupoRepository grupoRepository, ModelMapper modelMapper) {
    this.grupoRepository = grupoRepository;
    this.modelMapper = modelMapper;
  }

  @Override
  protected JpaRepository<Grupo, Long> getRepository() {
    return grupoRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<GrupoResponseDto> completeGrupo(String query) {
    if ("".equalsIgnoreCase(query)) {
      return grupoRepository.findAll()
              .stream()
              .map(this::convertToDto)
              .toList();
    } else {
      return grupoRepository.findByDescricaoLikeIgnoreCase("%" + query + "%")
              .stream()
              .map(this::convertToDto)
              .toList();
    }
  }

  @Override
  public GrupoResponseDto convertToDto(Grupo entity) {
    return modelMapper.map(entity, GrupoResponseDto.class);
  }
}
