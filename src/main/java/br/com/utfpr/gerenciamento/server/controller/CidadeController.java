package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Cidade;
import br.com.utfpr.gerenciamento.server.service.CidadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("cidade")
public class CidadeController {

    @Autowired
    private CidadeService cidadeService;

    @GetMapping
    public List<Cidade> findAll() {
        return cidadeService.findAll();
    }

    @GetMapping("{id}")
    public Cidade findOne(@PathVariable("id") Long id) {
        return cidadeService.findOne(id);
    }

    @PostMapping
    public Cidade save(@RequestBody Cidade cidade) {
        return cidadeService.save(cidade);
    }

    @GetMapping("/complete")
    public List<Cidade> complete(@RequestParam("query") String query) {
        return cidadeService.cidadeComplete(query);
    }
}
