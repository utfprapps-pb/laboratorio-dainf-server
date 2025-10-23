package br.com.utfpr.gerenciamento.server.model.dashboards;

import java.math.BigDecimal;

/**
 * DTO para itens adquiridos agregados por nome.
 *
 * @param qtde Quantidade total de aquisições do item
 * @param item Nome do item
 */
public record DashboardItensAdquiridos(BigDecimal qtde, String item) {}
