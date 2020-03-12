package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Cidade;
import br.com.utfpr.gerenciamento.server.service.CidadeService;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("cidade")
public class CidadeController extends CrudController<Cidade, Long> {

    @Autowired
    private CidadeService cidadeService;

    @Override
    protected CrudService<Cidade, Long> getService() {
        return cidadeService;
    }

    @GetMapping("/complete")
    public List<Cidade> complete(@RequestParam("query") String query) {
        return cidadeService.cidadeComplete(query);
    }
}
