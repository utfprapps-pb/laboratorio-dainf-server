package br.com.utfpr.gerenciamento.server.dto.dashboards;

import java.time.LocalDate;

/**
 * DTO de resposta para contagem de empréstimos por dia.
 *
 * @param qtde Quantidade de empréstimos
 * @param dtEmprestimo Data do empréstimo
 */
public record DashboardEmprestimoDiaResponseDto(Long qtde, LocalDate dtEmprestimo) {}
