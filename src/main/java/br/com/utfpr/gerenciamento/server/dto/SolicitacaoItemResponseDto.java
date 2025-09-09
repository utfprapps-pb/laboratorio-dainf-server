package br.com.utfpr.gerenciamento.server.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SolicitacaoItemResponseDto {
    private Long id;

    private BigDecimal qtde;

    private ItemResponseDto item;
}
