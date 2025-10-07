package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.dto.PaisResponseDto;
import br.com.utfpr.gerenciamento.server.model.Pais;
import br.com.utfpr.gerenciamento.server.repository.PaisRepository;
import br.com.utfpr.gerenciamento.server.service.PaisService;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
  @Cacheable(
      value = "paises",
      key = "#query == null || #query.isEmpty() ? 'all' : #query",
      unless = "#result.isEmpty()")
  public List<PaisResponseDto> paisComplete(String query) {
    // Cache agressivo: Lista de países raramente muda
    // TTL: 6 horas (configurado em CacheConfig)
    if ("".equalsIgnoreCase(query)) {
      return this.paisRepository.findAll().stream().map(this::convertToDto).toList();
    } else {
      return this.paisRepository.findByNomeLikeIgnoreCase("%" + query + "%").stream()
          .map(this::convertToDto)
          .toList();
    }
  }

  @Override
  @CacheEvict(value = "paises", allEntries = true)
  public Pais save(Pais pais) {
    // Limpa cache ao salvar país
    return super.save(pais);
  }

  @Override
  @CacheEvict(value = "paises", allEntries = true)
  public void delete(Long id) {
    // Limpa cache ao deletar país
    super.delete(id);
  }

  @Override
  public PaisResponseDto convertToDto(Pais entity) {
    return modelMapper.map(entity, PaisResponseDto.class);
  }
}
