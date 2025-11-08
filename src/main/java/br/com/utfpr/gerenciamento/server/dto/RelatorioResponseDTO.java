package br.com.utfpr.gerenciamento.server.dto;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RelatorioResponseDTO {
  private Long id;

  private String nome;

  private String nameReport;

  private List<RelatorioParamsResponseDTO> paramsList;
}
