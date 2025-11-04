package br.com.utfpr.gerenciamento.server.dto;

import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.Saida;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaidaItemResponseDTO {
  private Long id;

  private BigDecimal qtde;

  private Item item;

  private Saida saida;
}
