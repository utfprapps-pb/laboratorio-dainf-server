package br.com.utfpr.gerenciamento.server.model.dashboards;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardEmprestimoDia {

    private Long qtde;
    private LocalDate dtEmprestimo;
}
