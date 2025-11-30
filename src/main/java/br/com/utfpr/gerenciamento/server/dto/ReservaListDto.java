package br.com.utfpr.gerenciamento.server.dto;

import br.com.utfpr.gerenciamento.server.config.LocalDateDeserializer;
import br.com.utfpr.gerenciamento.server.config.LocalDateSerializer;
import br.com.utfpr.gerenciamento.server.repository.projection.ReservaListProjection;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO simplificado para listagem paginada de Reservas.
 *
 * <p>Contém apenas os campos necessários para exibição em tabelas no frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaListDto {

  private Long id;
  private String descricao;

  @JsonSerialize(using = LocalDateSerializer.class)
  @JsonDeserialize(using = LocalDateDeserializer.class)
  private LocalDate dataReserva;

  @JsonSerialize(using = LocalDateSerializer.class)
  @JsonDeserialize(using = LocalDateDeserializer.class)
  private LocalDate dataRetirada;

  private String nomeUsuario;

  /**
   * Converte uma projection JPA para DTO.
   *
   * @param projection projeção retornada pelo repositório
   * @return DTO com os dados mapeados
   */
  public static ReservaListDto fromProjection(ReservaListProjection projection) {
    if (projection == null) {
      return null;
    }
    return ReservaListDto.builder()
        .id(projection.getId())
        .descricao(projection.getDescricao())
        .dataReserva(projection.getDataReserva())
        .dataRetirada(projection.getDataRetirada())
        .nomeUsuario(projection.getUsuarioNome())
        .build();
  }
}
