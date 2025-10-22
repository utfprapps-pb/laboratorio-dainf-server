package br.com.utfpr.gerenciamento.server.model.dashboards;

import java.time.LocalDate;

/**
 * DTO para contagem de empréstimos agrupados por data.
 *
 * @param qtde Quantidade de empréstimos na data
 * @param dtEmprestimo Data do empréstimo
 */
public record DashboardEmprestimoDia(Long qtde, LocalDate dtEmprestimo) {}
