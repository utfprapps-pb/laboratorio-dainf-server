package br.com.utfpr.gerenciamento.server.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SolicitacaoItemResponseDto {
  private Long id;

  private BigDecimal qtde;

  private ItemResponseDto item;
}
