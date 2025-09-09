package br.com.utfpr.gerenciamento.server.dto.dashboards;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DashboardEmprestimoDiaResponseDto {

    private Long qtde;

    private LocalDate dtEmprestimo;
}
