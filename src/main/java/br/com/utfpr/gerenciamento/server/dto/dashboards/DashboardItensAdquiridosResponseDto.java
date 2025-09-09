package br.com.utfpr.gerenciamento.server.dto.dashboards;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardItensAdquiridosResponseDto {

    private BigDecimal qtde;

    private String item;
}
