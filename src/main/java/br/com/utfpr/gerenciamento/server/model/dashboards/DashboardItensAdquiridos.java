package br.com.utfpr.gerenciamento.server.model.dashboards;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardItensAdquiridos {

  private BigDecimal qtde;
  private String item;
}
