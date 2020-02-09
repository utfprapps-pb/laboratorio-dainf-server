package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Pais;
import br.com.utfpr.gerenciamento.server.service.PaisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("pais")
public class PaisController {

    @Autowired
    private PaisService paisService;

    @GetMapping
    public List<Pais> findAll() {
        return paisService.findAll();
    }

    @GetMapping("{id}")
    public Pais findOne(@PathVariable("id") Long id) {
        return paisService.findOne(id);
    }

    @PostMapping
    public Pais save(@RequestBody Pais pais) {
        return paisService.save(pais);
    }

    @GetMapping("/complete")
    public List<Pais> complete(@RequestParam("query") String query) {
        return paisService.paisComplete(query);
    }
}
