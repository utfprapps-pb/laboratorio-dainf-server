package br.com.utfpr.gerenciamento.server.dto;

import br.com.utfpr.gerenciamento.server.config.LocalDateDeserializer;
import br.com.utfpr.gerenciamento.server.config.LocalDateSerializer;
import br.com.utfpr.gerenciamento.server.repository.projection.CompraListProjection;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO simplificado para listagem paginada de Compras.
 *
 * <p>Contém apenas os campos necessários para exibição em tabelas no frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompraListDto {

  private Long id;

  @JsonSerialize(using = LocalDateSerializer.class)
  @JsonDeserialize(using = LocalDateDeserializer.class)
  private LocalDate dataCompra;

  private String fornecedorRazaoSocial;
  private String fornecedorNomeFantasia;

  /**
   * Converte uma projection JPA para DTO.
   *
   * @param projection projeção retornada pelo repositório
   * @return DTO com os dados mapeados
   */
  public static CompraListDto fromProjection(CompraListProjection projection) {
    if (projection == null) {
      return null;
    }
    return CompraListDto.builder()
        .id(projection.getId())
        .dataCompra(projection.getDataCompra())
        .fornecedorRazaoSocial(projection.getFornecedorRazaoSocial())
        .fornecedorNomeFantasia(projection.getFornecedorNomeFantasia())
        .build();
  }
}
