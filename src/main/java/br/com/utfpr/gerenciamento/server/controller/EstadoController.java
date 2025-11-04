package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.dto.EstadoResponseDto;
import br.com.utfpr.gerenciamento.server.model.Estado;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.EstadoService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("estado")
public class EstadoController extends CrudController<Estado, Long, EstadoResponseDto> {

  private final EstadoService estadoService;

  public EstadoController(EstadoService estadoService) {
    this.estadoService = estadoService;
  }

  @Override
  protected CrudService<Estado, Long, EstadoResponseDto> getService() {
    return estadoService;
  }

  @GetMapping("complete")
  public List<EstadoResponseDto> complete(@RequestParam("query") String query) {
    return estadoService.estadoComplete(query);
  }
}
