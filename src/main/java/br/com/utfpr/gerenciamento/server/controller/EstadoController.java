package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Estado;
import br.com.utfpr.gerenciamento.server.service.EstadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("estado")
public class EstadoController {

    @Autowired
    private EstadoService estadoService;

    @GetMapping
    public List<Estado> findAll() {
        return estadoService.findAll();
    }

    @GetMapping("/{id}")
    public Estado findOne(@PathVariable("id") Long id) {
        return estadoService.findOne(id);
    }

    @PostMapping
    public Estado save(@RequestBody Estado estado) {
        return estadoService.save(estado);
    }

    @GetMapping("complete")
    public List<Estado> complete(@RequestParam("query") String query) {
        return estadoService.estadoComplete(query);
    }
}
