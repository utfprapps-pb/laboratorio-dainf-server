package br.com.utfpr.gerenciamento.server.model.dashboards;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardItensAdquiridos {

    private BigDecimal qtde;
    private String item;
}
