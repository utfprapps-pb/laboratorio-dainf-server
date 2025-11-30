package br.com.utfpr.gerenciamento.server.controller;

import br.com.utfpr.gerenciamento.server.dto.ItemResponseDto;
import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.ItemImage;
import br.com.utfpr.gerenciamento.server.service.CrudService;
import br.com.utfpr.gerenciamento.server.service.ItemService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RestController
@RequestMapping("item")
public class ItemController extends CrudController<Item, Long, ItemResponseDto> {

  private final ItemService itemService;
  private List<ItemImage> imagesToCopy;

  public ItemController(ItemService itemService) {
    this.itemService = itemService;
  }

  @Override
  protected CrudService<Item, Long, ItemResponseDto> getService() {
    return itemService;
  }

  @Override
  public void preSave(Item object) {
    if (object.getId() == null
        && object.getImageItem() != null
        && !object.getImageItem().isEmpty()) {
      this.imagesToCopy = object.getImageItem();
      object.setImageItem(null);
    }
  }

  @Override
  @GetMapping("{id}")
  public ItemResponseDto findone(@PathVariable("id") Long id) {
    return itemService.toDto(itemService.findOneWithDisponibilidade(id));
  }

  @Override
  @GetMapping
  public List<ItemResponseDto> findAll() {
    return getService().findAll(Sort.by("id"));
  }

  /**
   * Lista paginada de itens com filtro textual usando DTO simplificado.
   *
   * <p><b>Otimização:</b> Retorna apenas campos necessários para listagem via projeção SQL.
   *
   * @param page Número da página (0-indexed)
   * @param size Tamanho da página
   * @param filter Filtro opcional (busca textual em todos os campos)
   * @param order Campo de ordenação (padrão: "id")
   * @param asc Direção da ordenação (true = ASC, false = DESC, padrão: ASC)
   * @return Página de itens simplificados
   */
  @Override
  @GetMapping("page")
  @SuppressWarnings("unchecked")
  public Page<ItemResponseDto> findAllPaged(
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
    return (Page<ItemResponseDto>) (Page<?>) itemService.findAllPagedList(filter, pageRequest);
  }

  @Override
  public void postSave(Item object) {
    if (this.imagesToCopy != null) {
      itemService.copyImagesItem(this.imagesToCopy, object.getId());
    }
    this.imagesToCopy = null;
  }

  @GetMapping("/complete")
  public List<ItemResponseDto> complete(
      @RequestParam("query") String query, @RequestParam("hasEstoque") Boolean hasEstoque) {
    // TODO: Migrar parâmetro para disponivelParaEmprestimo quando atualizar frontend
    return itemService.itemComplete(query, hasEstoque);
  }

  @PostMapping("upload-images")
  public void upload(
      @RequestParam("idItem") Long idItem,
      MultipartHttpServletRequest images,
      HttpServletRequest request) {
    if (images.getFile("anexos[]") != null) {
      itemService.saveImages(images, request, idItem);
    }
  }

  @GetMapping("imagens/{idItem}")
  public List<ItemImage> findAllImagesByItem(@PathVariable("idItem") Long idItem) {
    return itemService.getImagesItem(idItem);
  }

  @PostMapping("delete-image/{idItem}")
  public void deleteImageItem(
      @PathVariable("idItem") Long idItem, @RequestBody ItemImage itemImage) {
    itemService.deleteImage(itemImage, idItem);
  }
}
