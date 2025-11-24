package br.com.utfpr.gerenciamento.server.dto.dashboards;

import java.math.BigDecimal;

/**
 * DTO de resposta para itens com mais saídas.
 *
 * @param qtde Quantidade de saídas do item
 * @param item Nome do item
 */
public record DashboardItensSaidasResponseDto(BigDecimal qtde, String item) {}
