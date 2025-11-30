package br.com.utfpr.gerenciamento.server.dto;

import br.com.utfpr.gerenciamento.server.config.LocalDateDeserializer;
import br.com.utfpr.gerenciamento.server.config.LocalDateSerializer;
import br.com.utfpr.gerenciamento.server.repository.projection.SaidaListProjection;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO simplificado para listagem paginada de Saídas.
 *
 * <p>Contém apenas os campos necessários para exibição em tabelas no frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaidaListDto {

  private Long id;

  @JsonSerialize(using = LocalDateSerializer.class)
  @JsonDeserialize(using = LocalDateDeserializer.class)
  private LocalDate dataSaida;

  private String observacao;
  private String nomeUsuarioResponsavel;
  private java.math.BigDecimal qtdeTotal;

  /**
   * Converte uma projection JPA para DTO.
   *
   * @param projection projeção retornada pelo repositório
   * @return DTO com os dados mapeados
   */
  public static SaidaListDto fromProjection(SaidaListProjection projection) {
    if (projection == null) {
      return null;
    }
    return SaidaListDto.builder()
        .id(projection.getId())
        .dataSaida(projection.getDataSaida())
        .observacao(projection.getObservacao())
        .nomeUsuarioResponsavel(projection.getUsuarioResponsavelNome())
        .qtdeTotal(projection.getQtdeTotal())
        .build();
  }
}
