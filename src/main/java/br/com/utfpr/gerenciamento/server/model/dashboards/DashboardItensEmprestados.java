package br.com.utfpr.gerenciamento.server.model.dashboards;

import java.math.BigDecimal;

/**
 * DTO para itens mais emprestados agregados por nome.
 *
 * @param qtde Quantidade total de empréstimos do item
 * @param item Nome do item
 */
public record DashboardItensEmprestados(BigDecimal qtde, String item) {}
