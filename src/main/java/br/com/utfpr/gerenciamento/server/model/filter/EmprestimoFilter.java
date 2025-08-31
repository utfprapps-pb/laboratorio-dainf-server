package br.com.utfpr.gerenciamento.server.model.filter;

import br.com.utfpr.gerenciamento.server.model.Usuario;
import lombok.Data;

@Data
public class EmprestimoFilter {

  private Usuario usuarioEmprestimo;
  private Usuario usuarioResponsalvel;
  private String dtIniEmp;
  private String dtFimEmp;
  private String status;
}
