package br.com.utfpr.gerenciamento.server.dto.dashboards;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class DashboardItensSaidasResponseDto {

  private BigDecimal qtde;

  private String item;
}
