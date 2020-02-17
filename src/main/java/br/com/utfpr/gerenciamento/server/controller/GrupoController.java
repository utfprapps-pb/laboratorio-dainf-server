package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Grupo;
import br.com.utfpr.gerenciamento.server.service.GrupoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("grupo")
public class GrupoController {

    @Autowired
    private GrupoService grupoService;

    @GetMapping
    public List<Grupo> findAll() {
        return grupoService.findAll();
    }

    @GetMapping("{id}")
    public Grupo findOne(@PathVariable("id") Long id) {
        return grupoService.findOne(id);
    }

    @PostMapping
    public Grupo save(@RequestBody Grupo grupo) {
        return grupoService.save(grupo);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable("id") Long id) {
        grupoService.delete(id);
    }

    @GetMapping("/complete")
    public List<Grupo> complete(@RequestParam("query") String query) {
        return grupoService.completeGrupo(query);
    }
}
