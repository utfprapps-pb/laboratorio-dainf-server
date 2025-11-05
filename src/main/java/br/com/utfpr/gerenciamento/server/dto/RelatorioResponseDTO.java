package br.com.utfpr.gerenciamento.server.dto;

import lombok.*;
import java.util.List;

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
