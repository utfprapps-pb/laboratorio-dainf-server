package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.dto.ItemResponseDto;
import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.ItemImage;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.multipart.MultipartHttpServletRequest;

public interface ItemService extends CrudService<Item, Long> {

  List<ItemResponseDto> itemComplete(String query, Boolean hasEstoque);

  List<ItemResponseDto> findByGrupo(Long id);

  void diminuiSaldoItem(Long idItem, BigDecimal qtde, boolean needValidationSaldo);

  void aumentaSaldoItem(Long idItem, BigDecimal qtde);

  BigDecimal getSaldoItem(Long idItem);

  Boolean saldoItemIsValid(BigDecimal saldoItem, BigDecimal qtdeVerificar);

  void saveImages(MultipartHttpServletRequest files, HttpServletRequest request, Long idItem);

  List<ItemImage> getImagesItem(Long idItem);

  void deleteImage(ItemImage image, Long idItem);

  void sendNotificationItensAtingiramQtdeMin();

  void copyImagesItem(List<ItemImage> itemImages, Long id);

  ItemResponseDto convertToDto(Item entity);
}
