package br.com.utfpr.gerenciamento.server.dto;

import br.com.utfpr.gerenciamento.server.config.LocalDateDeserializer;
import br.com.utfpr.gerenciamento.server.config.LocalDateSerializer;
import br.com.utfpr.gerenciamento.server.repository.projection.SolicitacaoListProjection;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO simplificado para listagem paginada de Solicitações de Compra.
 *
 * <p>Contém apenas os campos necessários para exibição em tabelas no frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitacaoListDto {

  private Long id;
  private String descricao;

  @JsonSerialize(using = LocalDateSerializer.class)
  @JsonDeserialize(using = LocalDateDeserializer.class)
  private LocalDate dataSolicitacao;

  private String nomeUsuario;

  /**
   * Converte uma projection JPA para DTO.
   *
   * @param projection projeção retornada pelo repositório
   * @return DTO com os dados mapeados
   */
  public static SolicitacaoListDto fromProjection(SolicitacaoListProjection projection) {
    if (projection == null) {
      return null;
    }
    return SolicitacaoListDto.builder()
        .id(projection.getId())
        .descricao(projection.getDescricao())
        .dataSolicitacao(projection.getDataSolicitacao())
        .nomeUsuario(projection.getUsuarioNome())
        .build();
  }
}
