package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.dto.FornecedorResponseDto;
import br.com.utfpr.gerenciamento.server.model.Fornecedor;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.FornecedorService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("fornecedor")
public class FornecedorController extends CrudController<Fornecedor, Long, FornecedorResponseDto> {

  private final FornecedorService fornecedorService;

  public FornecedorController(FornecedorService fornecedorService) {
    this.fornecedorService = fornecedorService;
  }

  @Override
  protected CrudService<Fornecedor, Long, FornecedorResponseDto> getService() {
    return fornecedorService;
  }

  /**
   * Lista paginada de fornecedores com filtro textual usando DTO simplificado.
   *
   * <p><b>Otimização:</b> Retorna apenas campos necessários para listagem via projeção SQL.
   *
   * @param page Número da página (0-indexed)
   * @param size Tamanho da página
   * @param filter Filtro opcional (busca textual em todos os campos)
   * @param order Campo de ordenação (padrão: "id")
   * @param asc Direção da ordenação (true = ASC, false = DESC, padrão: ASC)
   * @return Página de fornecedores simplificados
   */
  @Override
  @GetMapping("page")
  @SuppressWarnings("unchecked")
  public Page<FornecedorResponseDto> findAllPaged(
      @RequestParam("page") int page,
      @RequestParam("size") int size,
      @RequestParam(required = false) String filter,
      @RequestParam(required = false) String order,
      @RequestParam(required = false) Boolean asc) {
    PageRequest pageRequest = PageRequest.of(page, size);
    if (order != null && asc != null) {
      pageRequest =
          PageRequest.of(page, size, asc ? Sort.Direction.ASC : Sort.Direction.DESC, order);
    }
    return (Page<FornecedorResponseDto>)
        (Page<?>) fornecedorService.findAllPagedList(filter, pageRequest);
  }

  @GetMapping("/complete")
  public List<FornecedorResponseDto> complete(@RequestParam("query") String query) {
    return fornecedorService.completeFornecedor(query).stream()
        .map(fornecedorService::toDto)
        .toList();
  }
}
