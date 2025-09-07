package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Solicitacao;
import br.com.utfpr.gerenciamento.server.repository.SolicitacaoRepository;
import br.com.utfpr.gerenciamento.server.service.SolicitacaoService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SolicitacaoServiceImpl extends CrudServiceImpl<Solicitacao, Long>
    implements SolicitacaoService {

  private final SolicitacaoRepository solicitacaoRepository;
  private final UsuarioService usuarioService;

  public SolicitacaoServiceImpl(
      SolicitacaoRepository solicitacaoRepository, UsuarioService usuarioService) {
    this.solicitacaoRepository = solicitacaoRepository;
    this.usuarioService = usuarioService;
  }

  @Override
  protected JpaRepository<Solicitacao, Long> getRepository() {
    return solicitacaoRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Solicitacao> findAllByUsername(String username) {
    var usuario = usuarioService.findByUsername(username);
    return solicitacaoRepository.findAllByUsuario(usuario);
  }
}
