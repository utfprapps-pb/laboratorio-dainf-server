package br.com.utfpr.gerenciamento.server.model.filter;

import br.com.utfpr.gerenciamento.server.enumeration.EmprestimoStatus;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

/**
 * Filtro para consultas de empréstimos.
 *
 * <p>Mantém @JsonAlias e @JsonSetter para backward compatibility com APIs públicas. TODO: Alterar
 * frontend para campos corrigidos e remover isso aqui
 */
@Data
public class EmprestimoFilter {

  private Usuario usuarioEmprestimo;

  @JsonAlias("usuarioResponsalvel")
  private Usuario usuarioResponsavel;

  // Campos legados mantidos para backward compatibility via JSON
  private String dtIniEmp;
  private String dtFimEmp;

  @JsonIgnore private DateRange dateRange;

  private EmprestimoStatus status;

  /**
   * Setter customizado para aceitar tanto String quanto enum.
   *
   * <p>Permite backward compatibility: API pode enviar "A", "P", "F", "T" como String e serão
   * convertidos para enum automaticamente.
   */
  @JsonSetter("status")
  public void setStatusFromString(String statusCodigo) {
    this.status = EmprestimoStatus.fromCodigoOrNull(statusCodigo);
  }

  /**
   * Retorna DateRange construído a partir de dtIniEmp/dtFimEmp ou campo dateRange direto.
   *
   * <p>Prioriza dateRange se já setado, caso contrário constrói a partir dos campos String legacy.
   */
  @JsonIgnore
  public DateRange getDateRangeEmprestimo() {
    if (dateRange != null) {
      return dateRange;
    }

    return DateRange.fromStrings(dtIniEmp, dtFimEmp);
  }
}
