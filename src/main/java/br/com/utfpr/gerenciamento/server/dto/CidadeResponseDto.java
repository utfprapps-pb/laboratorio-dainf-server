package br.com.utfpr.gerenciamento.server.dto;

import lombok.Data;

@Data
public class CidadeResponseDto {

  private Long id;

  private String nome;

  private EstadoResponseDto estado;
}
