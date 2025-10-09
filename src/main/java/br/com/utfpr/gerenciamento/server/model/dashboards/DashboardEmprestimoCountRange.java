package br.com.utfpr.gerenciamento.server.model.dashboards;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para contadores de empréstimos agregados por status.
 *
 * <p>Usa Long para evitar overflow em sistemas de longo prazo com muitos registros.
 * Math.toIntExact() anterior poderia lançar ArithmeticException se count > 2.1B.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardEmprestimoCountRange {

  private Long total;
  private Long emAndamento;
  private Long emAtraso;
  private Long finalizado;
}
