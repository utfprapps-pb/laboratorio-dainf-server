package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Estado;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.EstadoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("estado")
public class EstadoController extends CrudController<Estado, Long>{

    private final EstadoService estadoService;

    public EstadoController(EstadoService estadoService) {
        this.estadoService = estadoService;
    }

    @Override
    protected CrudService<Estado, Long> getService() {
        return estadoService;
    }

    @GetMapping("complete")
    public List<Estado> complete(@RequestParam("query") String query) {
        return estadoService.estadoComplete(query);
    }
}
