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
}
