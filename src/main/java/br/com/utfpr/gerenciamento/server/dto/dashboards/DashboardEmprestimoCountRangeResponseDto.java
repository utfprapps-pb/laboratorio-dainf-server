package br.com.utfpr.gerenciamento.server.dto.dashboards;

import lombok.Data;

@Data
public class DashboardEmprestimoCountRangeResponseDto {
    private Integer total;

    private Integer emAndamento;

    private Integer emAtraso;

    private Integer finalizado;
}
