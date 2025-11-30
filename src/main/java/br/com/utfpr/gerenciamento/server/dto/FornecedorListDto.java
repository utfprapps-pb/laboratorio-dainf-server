package br.com.utfpr.gerenciamento.server.dto;

import br.com.utfpr.gerenciamento.server.repository.projection.FornecedorListProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO simplificado para listagem paginada de Fornecedores.
 *
 * <p>Contém apenas os campos necessários para exibição em tabelas no frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FornecedorListDto {

  private Long id;
  private String razaoSocial;
  private String nomeFantasia;
  private String cnpj;

  /**
   * Converte uma projection JPA para DTO.
   *
   * @param projection projeção retornada pelo repositório
   * @return DTO com os dados mapeados
   */
  public static FornecedorListDto fromProjection(FornecedorListProjection projection) {
    if (projection == null) {
      return null;
    }
    return FornecedorListDto.builder()
        .id(projection.getId())
        .razaoSocial(projection.getRazaoSocial())
        .nomeFantasia(projection.getNomeFantasia())
        .cnpj(projection.getCnpj())
        .build();
  }
}
