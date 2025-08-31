package br.com.utfpr.gerenciamento.server.model.dashboards;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardEmprestimoDia {

  private Long qtde;
  private LocalDate dtEmprestimo;
}
