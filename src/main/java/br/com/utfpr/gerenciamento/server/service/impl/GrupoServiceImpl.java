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
public class GrupoServiceImpl extends CrudServiceImpl<Grupo, Long, GrupoResponseDto>
    implements GrupoService {

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
  public GrupoResponseDto toDto(Grupo entity) {
    return modelMapper.map(entity, GrupoResponseDto.class);
  }

  @Override
  public Grupo toEntity(GrupoResponseDto grupoResponseDto) {
    return modelMapper.map(grupoResponseDto, Grupo.class);
  }

  @Override
  @Transactional(readOnly = true)
  public List<GrupoResponseDto> completeGrupo(String query) {
    if (query == null || query.isBlank()) {
      return grupoRepository.findAll().stream().map(this::toDto).toList();
    } else {
      final String newQuery = query.trim();
      return grupoRepository.findByDescricaoLikeIgnoreCase("%" + newQuery + "%").stream()
          .map(this::toDto)
          .toList();
    }
  }
}
