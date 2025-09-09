package br.com.utfpr.gerenciamento.server.dto;

import br.com.utfpr.gerenciamento.server.model.Item;
import br.com.utfpr.gerenciamento.server.model.Solicitacao;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SolicitacaoItemResponseDto {
    private Long id;

    private BigDecimal qtde;

    private Item item;

    private Solicitacao solicitacao;
}
