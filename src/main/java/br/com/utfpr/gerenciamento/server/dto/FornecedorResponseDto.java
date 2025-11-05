package br.com.utfpr.gerenciamento.server.dto;

import lombok.Data;

@Data
public class FornecedorResponseDto {

  Long id;

  private String razaoSocial;

  private String nomeFantasia;

  private String cnpj;



}
