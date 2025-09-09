package br.com.utfpr.gerenciamento.server.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class SolicitacaoResponseDto {
    private Long id;

    private String descricao;

    private LocalDate dataSolicitacao;

    private UsuarioResponseDto usuario;

    private List<SolicitacaoItemResponseDto> solicitacaoItem;

    private String observacao;
}
