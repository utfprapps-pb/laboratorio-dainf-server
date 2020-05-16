package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Solicitacao;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.SolicitacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("solicitacao-compra")
public class SolicitacaoController extends CrudController<Solicitacao, Long> {

    @Autowired
    private SolicitacaoService solicitacaoService;

    @Override
    protected CrudService<Solicitacao, Long> getService() {
        return solicitacaoService;
    }

    @GetMapping("find-all-by-username/{username}")
    public List<Solicitacao> findAllByUsername(@PathVariable("username") String username) {
        return solicitacaoService.findAllByUsername(username);
    }
}
