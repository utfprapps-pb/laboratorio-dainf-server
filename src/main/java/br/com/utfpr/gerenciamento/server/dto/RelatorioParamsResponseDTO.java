package br.com.utfpr.gerenciamento.server.dto;

import br.com.utfpr.gerenciamento.server.enumeration.TypeParamReport;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioParamsResponseDTO {
  private Long id;

  private String nameParam;

  private String aliasParam;

  private TypeParamReport tipoParam;

  private RelatorioResponseDTO relatorio;
}
