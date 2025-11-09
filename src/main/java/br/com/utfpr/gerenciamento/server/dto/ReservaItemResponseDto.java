package br.com.utfpr.gerenciamento.server.dto;

import java.math.BigDecimal;

import br.com.utfpr.gerenciamento.server.model.Reserva;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

@Data
public class ReservaItemResponseDto {
  private Long id;

  private BigDecimal qtde;

  private ItemResponseDto item;
  @JsonBackReference
  private ReservaResponseDto reserva;
}
