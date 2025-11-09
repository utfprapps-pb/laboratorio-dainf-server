package br.com.utfpr.gerenciamento.server.factory;

import br.com.utfpr.gerenciamento.server.enumeration.TipoItem;
import br.com.utfpr.gerenciamento.server.model.Grupo;
import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.repository.projection.ItemCompleteWithDisponibilidade;
import br.com.utfpr.gerenciamento.server.repository.projection.ItemWithQtdeEmprestada;
import java.math.BigDecimal;

/**
 * Factory para criação de objetos de teste relacionados a Item. Elimina código duplicado e
 * padroniza a criação de objetos de teste.
 */
public class ItemFactory {

  public static Item createItemPadrao() {
    Item item = new Item();
    item.setId(1L);
    item.setNome("Notebook Dell");
    item.setTipoItem(TipoItem.P);
    item.setSaldo(new BigDecimal("10.00"));
    item.setQtdeMinima(new BigDecimal("2.00"));
    item.setDescricao("Notebook Dell 15 polegadas");
    return item;
  }

  public static Item createItem(Long id, String nome, TipoItem tipoItem, BigDecimal saldo) {
    Item item = new Item();
    item.setId(id);
    item.setNome(nome);
    item.setTipoItem(tipoItem);
    item.setSaldo(saldo);
    item.setQtdeMinima(new BigDecimal("1.00"));
    item.setDescricao("Descrição do item " + nome);
    return item;
  }

  public static Item createItemConsumivel(Long id, String nome, BigDecimal saldo) {
    return createItem(id, nome, TipoItem.C, saldo);
  }

  public static Item createItemPermanente(Long id, String nome, BigDecimal saldo) {
    return createItem(id, nome, TipoItem.P, saldo);
  }

  public static ItemWithQtdeEmprestada createItemWithQtdeEmprestada(
      Item item, BigDecimal qtdeEmprestada) {
    return new ItemWithQtdeEmprestada() {
      @Override
      public Item getItem() {
        return item;
      }

      @Override
      public BigDecimal getQtdeEmprestada() {
        return qtdeEmprestada;
      }
    };
  }

  public static ItemCompleteWithDisponibilidade createItemCompleteWithDisponibilidade(
      Long id, String nome, TipoItem tipoItem, BigDecimal saldo, BigDecimal qtdeEmprestada) {
    return new ItemCompleteWithDisponibilidade() {
      @Override
      public Long getId() {
        return id;
      }

      @Override
      public String getNome() {
        return nome;
      }

      @Override
      public BigDecimal getSaldo() {
        return saldo;
      }

      @Override
      public TipoItem getTipoItem() {
        return tipoItem;
      }

      @Override
      public BigDecimal getQtdeEmprestada() {
        return qtdeEmprestada;
      }

      @Override
      public Grupo getGrupo() {
        // Create a simple grupo for testing
        Grupo grupo = new Grupo();
        grupo.setId(1L);
        grupo.setDescricao("Grupo Teste");
        return grupo;
      }
    };
  }

  public static ItemCompleteWithDisponibilidade createItemCompleteAvailable(
      Long id, String nome, TipoItem tipoItem, BigDecimal saldo, BigDecimal qtdeEmprestada) {
    // Garante que o item está disponível para empréstimo
    if (tipoItem == TipoItem.P) {
      // Para permanentes: saldo > qtdeEmprestada
      BigDecimal disponivel = saldo.subtract(qtdeEmprestada);
      if (disponivel.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("Item permanente não está disponível");
      }
    }
    // Para consumíveis: só verifica saldo > 0

    return createItemCompleteWithDisponibilidade(id, nome, tipoItem, saldo, qtdeEmprestada);
  }
}
