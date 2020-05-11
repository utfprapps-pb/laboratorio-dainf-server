package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Solicitacao;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.SolicitacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("solicitacao-compra")
public class SolicitacaoController extends CrudController<Solicitacao, Long> {

    @Autowired
    private SolicitacaoService solicitacaoService;

    @Override
    protected CrudService<Solicitacao, Long> getService() {
        return solicitacaoService;
    }
}
