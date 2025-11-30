package br.com.utfpr.gerenciamento.server.dto;

import br.com.utfpr.gerenciamento.server.repository.projection.UsuarioListProjection;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO simplificado para listagem paginada de Usuários.
 *
 * <p>Contém apenas os campos necessários para exibição em tabelas no frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioListDto {

  private Long id;
  private String nome;
  private String username;
  private List<PermissaoListDto> permissoes;

  /** DTO aninhado para permissões. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class PermissaoListDto {
    private Long id;
    private String nome;

    /**
     * Converte uma projection de permissão para DTO.
     *
     * @param projection projeção retornada pelo repositório
     * @return DTO com os dados mapeados
     */
    public static PermissaoListDto fromProjection(
        UsuarioListProjection.PermissaoProjection projection) {
      if (projection == null) {
        return null;
      }
      return PermissaoListDto.builder().id(projection.getId()).nome(projection.getNome()).build();
    }
  }

  /**
   * Converte uma projection JPA para DTO.
   *
   * @param projection projeção retornada pelo repositório
   * @return DTO com os dados mapeados
   */
  public static UsuarioListDto fromProjection(UsuarioListProjection projection) {
    if (projection == null) {
      return null;
    }

    List<PermissaoListDto> permissoesList = null;
    Set<UsuarioListProjection.PermissaoProjection> permissoes = projection.getPermissoes();
    if (permissoes != null) {
      permissoesList = permissoes.stream().map(PermissaoListDto::fromProjection).toList();
    }

    return UsuarioListDto.builder()
        .id(projection.getId())
        .nome(projection.getNome())
        .username(projection.getUsername())
        .permissoes(permissoesList)
        .build();
  }
}
