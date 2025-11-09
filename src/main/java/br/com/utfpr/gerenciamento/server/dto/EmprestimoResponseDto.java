package br.com.utfpr.gerenciamento.server.dto;

import java.time.LocalDate;
import java.util.List;

import br.com.utfpr.gerenciamento.server.config.LocalDateDeserializer;
import br.com.utfpr.gerenciamento.server.config.LocalDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class EmprestimoResponseDto {
  private Long id;
  @JsonSerialize(using = LocalDateSerializer.class)
  @JsonDeserialize(using = LocalDateDeserializer.class)
  private LocalDate dataEmprestimo;
  @JsonSerialize(using = LocalDateSerializer.class)
  @JsonDeserialize(using = LocalDateDeserializer.class)
  private LocalDate prazoDevolucao;
  @JsonSerialize(using = LocalDateSerializer.class)
  @JsonDeserialize(using = LocalDateDeserializer.class)
  private LocalDate dataDevolucao;

  private UsuarioResponseDto usuarioResponsavel;

  private UsuarioResponseDto usuarioEmprestimo;

  private String observacao;

  private List<EmprestimoItemResponseDto> emprestimoItem;

  private List<EmprestimoDevolucaoItemResponseDto> emprestimoDevolucaoItem;
}
