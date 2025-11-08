package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.dto.GrupoResponseDto;
import br.com.utfpr.gerenciamento.server.dto.ItemResponseDto;
import br.com.utfpr.gerenciamento.server.model.Grupo;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.GrupoService;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("grupo")
public class GrupoController extends CrudController<Grupo, Long, GrupoResponseDto> {

  private final GrupoService grupoService;
  private final ItemService itemService;

  public GrupoController(GrupoService grupoService, ItemService itemService) {
    this.grupoService = grupoService;
    this.itemService = itemService;
  }

  @Override
  protected CrudService<Grupo, Long, GrupoResponseDto> getService() {
    return grupoService;
  }

  @GetMapping("/complete")
  public List<GrupoResponseDto> complete(@RequestParam("query") String query) {
    return grupoService.completeGrupo(query);
  }

  @GetMapping("/itens-vinculados/{idGrupo}")
  public List<ItemResponseDto> findItensVinculado(@PathVariable("idGrupo") Long idGrupo) {
    return itemService.findByGrupo(idGrupo);
  }
}
