package br.com.utfpr.gerenciamento.server.dto;

import br.com.utfpr.gerenciamento.server.enumeration.StatusDevolucao;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
public class EmprestimoDevolucaoItemResponseDto {
  private Long id;

  private BigDecimal qtde;

  private StatusDevolucao statusDevolucao;

  private ItemResponseDto item;

  @JsonIgnore @ToString.Exclude @EqualsAndHashCode.Exclude private EmprestimoResponseDto emprestimo;
}
