package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.model.Item;

import java.math.BigDecimal;
import java.util.List;

public interface ItemService extends CrudService<Item, Long> {

    List<Item> itemComplete(String query, Boolean hasEstoque);

    List<Item> findByGrupo(Long id);

    void diminuiSaldoItem(Long idItem, BigDecimal qtde);

    void aumentaSaldoItem(Long idItem, BigDecimal qtde);

    BigDecimal getSaldoItem(Long idItem);

    Boolean saldoItemIsValid(BigDecimal saldoItem, BigDecimal qtdeVerificar);

}
