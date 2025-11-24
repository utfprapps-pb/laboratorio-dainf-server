package br.com.utfpr.gerenciamento.server.model.dashboards;

import java.math.BigDecimal;

/**
 * DTO para itens com mais saídas agregados por nome.
 *
 * @param qtde Quantidade total de saídas do item
 * @param item Nome do item
 */
public record DashboardItensSaidas(BigDecimal qtde, String item) {}
