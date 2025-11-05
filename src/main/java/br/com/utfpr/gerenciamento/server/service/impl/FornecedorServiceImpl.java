package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.dto.FornecedorResponseDto;
import br.com.utfpr.gerenciamento.server.model.Fornecedor;
import br.com.utfpr.gerenciamento.server.repository.FornecedorRepository;
import br.com.utfpr.gerenciamento.server.service.FornecedorService;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FornecedorServiceImpl extends CrudServiceImpl<Fornecedor, Long, FornecedorResponseDto>
    implements FornecedorService {

  private final FornecedorRepository fornecedorRepository;

  private final ModelMapper modelMapper;

  public FornecedorServiceImpl(FornecedorRepository fornecedorRepository, ModelMapper modelMapper) {
    this.fornecedorRepository = fornecedorRepository;
    this.modelMapper = modelMapper;
  }

  @Override
  protected JpaRepository<Fornecedor, Long> getRepository() {
    return fornecedorRepository;
  }

  @Override
  public FornecedorResponseDto toDto(Fornecedor entity) {
    return modelMapper.map(entity, FornecedorResponseDto.class);
  }

  @Override
  public Fornecedor toEntity(FornecedorResponseDto fornecedorResponseDto) {
    return modelMapper.map(fornecedorResponseDto, Fornecedor.class);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Fornecedor> completeFornecedor(String query) {
    if ("".equalsIgnoreCase(query)) {
      return fornecedorRepository.findAll();
    } else {
      return fornecedorRepository.findByNomeFantasiaLikeIgnoreCase("%" + query + "%");
    }
  }

}
