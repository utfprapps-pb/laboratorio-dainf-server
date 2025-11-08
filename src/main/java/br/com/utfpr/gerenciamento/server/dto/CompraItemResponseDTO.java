package br.com.utfpr.gerenciamento.server.dto;

import br.com.utfpr.gerenciamento.server.model.Compra;
import br.com.utfpr.gerenciamento.server.model.Item;
import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompraItemResponseDTO {

  private Long id;

  private BigDecimal qtde;

  private BigDecimal valor;

  private Item item;

  private Compra compra;
}
