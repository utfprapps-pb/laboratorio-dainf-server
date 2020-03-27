package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Saida;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.SaidaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("saida")
public class SaidaController extends CrudController<Saida, Long> {

    @Autowired
    private SaidaService saidaService;

    @Override
    protected CrudService<Saida, Long> getService() {
        return saidaService;
    }
}
