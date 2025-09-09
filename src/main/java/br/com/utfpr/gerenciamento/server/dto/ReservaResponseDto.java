package br.com.utfpr.gerenciamento.server.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ReservaResponseDto {
    private Long id;

    private String descricao;

    private LocalDate dataReserva;

    private LocalDate dataRetirada;

    private String observacao;

    private UsuarioResponseDto usuario;

    private List<ReservaItemResponseDto> reservaItem;
}
