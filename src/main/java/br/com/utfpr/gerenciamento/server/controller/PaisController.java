package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.dto.PaisResponseDto;
import br.com.utfpr.gerenciamento.server.model.Pais;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.PaisService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

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
  public List<PaisResponseDto> complete(@RequestParam("query") String query) {
    return paisService.paisComplete(query);
  }
}
