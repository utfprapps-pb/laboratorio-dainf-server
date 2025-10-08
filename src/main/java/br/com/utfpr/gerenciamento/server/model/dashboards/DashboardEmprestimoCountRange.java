package br.com.utfpr.gerenciamento.server.model.dashboards;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardEmprestimoCountRange {

  private Integer total;
  private Integer emAndamento;
  private Integer emAtraso;
  private Integer finalizado;

  // Construtor adicional para aceitar Long (retorno de COUNT/SUM do JPA)
  public DashboardEmprestimoCountRange(
      Long total, Long emAndamento, Long emAtraso, Long finalizado) {
    this.total = total != null ? Math.toIntExact(total) : 0;
    this.emAndamento = emAndamento != null ? Math.toIntExact(emAndamento) : 0;
    this.emAtraso = emAtraso != null ? Math.toIntExact(emAtraso) : 0;
    this.finalizado = finalizado != null ? Math.toIntExact(finalizado) : 0;
  }
}
