package br.com.utfpr.gerenciamento.server.dto;

import br.com.utfpr.gerenciamento.server.ennumeation.StatusDevolucao;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmprestimoDevolucaoItemResponseDto {
    private Long id;

    private BigDecimal qtde;

    private StatusDevolucao statusDevolucao;

    private ItemResponseDto item;

    private EmprestimoResponseDto emprestimo;
}
