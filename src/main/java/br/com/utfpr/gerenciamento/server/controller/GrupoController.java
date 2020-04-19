package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Grupo;
import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.GrupoService;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("grupo")
public class GrupoController extends CrudController<Grupo, Long> {

    @Autowired
    private GrupoService grupoService;
    @Autowired
    private ItemService itemService;

    @Override
    protected CrudService<Grupo, Long> getService() {
        return grupoService;
    }

    @GetMapping("/complete")
    public List<Grupo> complete(@RequestParam("query") String query) {
        return grupoService.completeGrupo(query);
    }

    @GetMapping("/itens-vinculados/{idGrupo}")
    public List<Item> findItensVinculado(@PathVariable("idGrupo") Long idGrupo) {
        return itemService.findByGrupo(idGrupo);
    }
}
