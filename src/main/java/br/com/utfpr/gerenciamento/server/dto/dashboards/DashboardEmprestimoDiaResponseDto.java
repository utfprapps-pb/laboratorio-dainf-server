package br.com.utfpr.gerenciamento.server.dto.dashboards;

import java.time.LocalDate;
import lombok.Data;

@Data
public class DashboardEmprestimoDiaResponseDto {

  private Long qtde;

  private LocalDate dtEmprestimo;
}
