package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.dto.EstadoResponseDto;
import br.com.utfpr.gerenciamento.server.model.Estado;
import br.com.utfpr.gerenciamento.server.repository.EstadoRepository;
import br.com.utfpr.gerenciamento.server.service.EstadoService;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstadoServiceImpl extends CrudServiceImpl<Estado, Long,EstadoResponseDto> implements EstadoService {

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
  public EstadoResponseDto toDto(Estado entity) {
    return modelMapper.map(entity, EstadoResponseDto.class);
  }

  @Override
  public Estado toEntity(EstadoResponseDto estadoResponseDto) {
    return modelMapper.map(estadoResponseDto, Estado.class);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(
      value = "estados",
      key = "#query == null ? 'all' : #query",
      unless = "#result.isEmpty()")
  public List<EstadoResponseDto> estadoComplete(String query) {
    // Cache agressivo: Estados brasileiros (27) raramente mudam
    // TTL: 6 horas (configurado em CacheConfig)
    if (query == null || query.isBlank()) {
      return estadoRepository.findAll().stream().map(this::toDto).toList();
    } else {
      return estadoRepository.findByNomeLikeIgnoreCase("%" + query + "%").stream()
          .map(this::toDto)
          .toList();
    }
  }

  @Override
  @CacheEvict(value = "estados", allEntries = true)
  public EstadoResponseDto save(Estado estado) {
    // Limpa cache ao salvar estado
    return super.save(estado);
  }

  @Override
  @CacheEvict(value = "estados", allEntries = true)
  public void delete(Long id) {
    // Limpa cache ao deletar estado
    super.delete(id);
  }
}
