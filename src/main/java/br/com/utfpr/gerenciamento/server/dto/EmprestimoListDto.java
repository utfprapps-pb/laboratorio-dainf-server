package br.com.utfpr.gerenciamento.server.dto;

import br.com.utfpr.gerenciamento.server.config.LocalDateDeserializer;
import br.com.utfpr.gerenciamento.server.config.LocalDateSerializer;
import br.com.utfpr.gerenciamento.server.repository.projection.EmprestimoListProjection;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO simplificado para listagem paginada de Empréstimos.
 *
 * <p>Contém apenas os campos necessários para exibição em tabelas no frontend, evitando o overhead
 * de carregar entidades completas com relacionamentos aninhados.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmprestimoListDto {

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

  private String nomeUsuarioEmprestimo;

  /**
   * Converte uma projection JPA para DTO.
   *
   * @param projection projeção retornada pelo repositório
   * @return DTO com os dados mapeados
   */
  public static EmprestimoListDto fromProjection(EmprestimoListProjection projection) {
    if (projection == null) {
      return null;
    }
    return EmprestimoListDto.builder()
        .id(projection.getId())
        .dataEmprestimo(projection.getDataEmprestimo())
        .prazoDevolucao(projection.getPrazoDevolucao())
        .dataDevolucao(projection.getDataDevolucao())
        .nomeUsuarioEmprestimo(projection.getUsuarioEmprestimoNome())
        .build();
  }
}
