package br.com.utfpr.gerenciamento.server.dto;

import br.com.utfpr.gerenciamento.server.model.Cidade;
import br.com.utfpr.gerenciamento.server.model.Estado;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FornecedorResponseDto {

  Long id;

  private String razaoSocial;

  private String nomeFantasia;

  private String cnpj;
  private String ie;
  private String endereco;
  private String observacao;
  private String email;
  private String telefone;
  private CidadeResponseDto cidade;
  private EstadoResponseDto estado;
}
