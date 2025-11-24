package br.com.utfpr.gerenciamento.server.dto;

import lombok.Data;

@Data
public class EstadoResponseDto {

  private Long id;

  private String nome;

  private String uf;

  private PaisResponseDto pais;
}
