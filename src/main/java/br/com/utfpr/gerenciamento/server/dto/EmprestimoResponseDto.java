package br.com.utfpr.gerenciamento.server.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class EmprestimoResponseDto {
  private Long id;

  private LocalDate dataEmprestimo;

  private LocalDate prazoDevolucao;

  private LocalDate dataDevolucao;

  private UsuarioResponseDto usuarioResponsavel;

  private UsuarioResponseDto usuarioEmprestimo;

  private String observacao;

  private List<EmprestimoItemResponseDto> emprestimoItem;

  private List<EmprestimoDevolucaoItemResponseDto> emprestimoDevolucaoItem;
}
