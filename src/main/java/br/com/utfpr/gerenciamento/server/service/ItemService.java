package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.model.Item;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

public interface ItemService extends CrudService<Item, Long> {

    List<Item> itemComplete(String query, Boolean hasEstoque);

    List<Item> findByGrupo(Long id);

    void diminuiSaldoItem(Long idItem, BigDecimal qtde, boolean needValidationSaldo);

    void aumentaSaldoItem(Long idItem, BigDecimal qtde);

    BigDecimal getSaldoItem(Long idItem);

    Boolean saldoItemIsValid(BigDecimal saldoItem, BigDecimal qtdeVerificar);

    void saveImages(MultipartHttpServletRequest files,
                    HttpServletRequest request,
                    Long idItem);
}
