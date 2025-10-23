package br.com.utfpr.gerenciamento.server.dto.dashboards;

import java.math.BigDecimal;

/**
 * DTO de resposta para itens mais adquiridos.
 *
 * @param qtde Quantidade adquirida do item
 * @param item Nome do item
 */
public record DashboardItensAdquiridosResponseDto(BigDecimal qtde, String item) {}
