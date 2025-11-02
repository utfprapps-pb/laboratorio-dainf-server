package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.dto.FornecedorResponseDto;
import br.com.utfpr.gerenciamento.server.model.Fornecedor;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.FornecedorService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("fornecedor")
public class FornecedorController extends CrudController<Fornecedor, Long,FornecedorResponseDto> {

  private final FornecedorService fornecedorService;

  public FornecedorController(FornecedorService fornecedorService) {
    this.fornecedorService = fornecedorService;
  }

  @Override
  protected CrudService<Fornecedor, Long,FornecedorResponseDto> getService() {
    return fornecedorService;
  }

  @GetMapping("/complete")
  public List<FornecedorResponseDto> complete(@RequestParam("query") String query) {
    return fornecedorService.completeFornecedor(query).stream()
        .map(fornecedorService::convertToDto)
        .toList();
  }
}
