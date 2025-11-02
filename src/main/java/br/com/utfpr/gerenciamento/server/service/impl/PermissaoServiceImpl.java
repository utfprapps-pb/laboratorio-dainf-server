package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.dto.PermissaoResponseDTO;
import br.com.utfpr.gerenciamento.server.model.Permissao;
import br.com.utfpr.gerenciamento.server.repository.PermissaoRepository;
import br.com.utfpr.gerenciamento.server.service.PermissaoService;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermissaoServiceImpl extends CrudServiceImpl<Permissao, Long, PermissaoResponseDTO>
    implements PermissaoService {

  private final PermissaoRepository permissaoRepository;
  private final ModelMapper modelMapper;

  public PermissaoServiceImpl(PermissaoRepository permissaoRepository, ModelMapper modelMapper) {
    this.permissaoRepository = permissaoRepository;
      this.modelMapper = modelMapper;
  }

  @Override
  protected JpaRepository<Permissao, Long> getRepository() {
    return permissaoRepository;
  }

  @Override
  public PermissaoResponseDTO toDto(Permissao entity) {
    return modelMapper.map(entity, PermissaoResponseDTO.class);
  }

  @Override
  public Permissao toEntity(PermissaoResponseDTO permissaoResponseDTO) {
    return modelMapper.map(permissaoResponseDTO, Permissao.class);
  }

  @Override
  @Transactional(readOnly = true)
  public Permissao findByNome(String nome) {
    return permissaoRepository.findByNome(nome);
  }
}
