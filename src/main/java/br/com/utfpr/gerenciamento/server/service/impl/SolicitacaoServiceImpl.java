package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Solicitacao;
import br.com.utfpr.gerenciamento.server.repository.SolicitacaoRepository;
import br.com.utfpr.gerenciamento.server.service.SolicitacaoService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SolicitacaoServiceImpl extends CrudServiceImpl<Solicitacao, Long> implements SolicitacaoService {

    @Autowired
    private SolicitacaoRepository solicitacaoRepository;
    @Autowired
    private UsuarioService usuarioService;

    @Override
    protected JpaRepository<Solicitacao, Long> getRepository() {
        return solicitacaoRepository;
    }

    @Override
    public List<Solicitacao> findAllByUsername(String username) {
        var usuario = usuarioService.findByUsername(username);
        return solicitacaoRepository.findAllByUsuario(usuario);
    }
}
