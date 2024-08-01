package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.model.Pais;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.PaisService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("pais")
public class PaisController extends CrudController<Pais, Long> {

    private final PaisService paisService;

    public PaisController(PaisService paisService) {
        this.paisService = paisService;
    }

    @Override
    protected CrudService<Pais, Long> getService() {
        return paisService;
    }

    @GetMapping("/complete")
    public List<Pais> complete(@RequestParam("query") String query) {
        return paisService.paisComplete(query);
    }
}
