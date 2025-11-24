package br.com.utfpr.gerenciamento.server.dto.dashboards;

import java.math.BigDecimal;

/**
 * DTO de resposta para itens mais emprestados.
 *
 * @param qtde Quantidade de empréstimos do item
 * @param item Nome do item
 */
public record DashboardItensEmprestadosResponseDto(BigDecimal qtde, String item) {}
