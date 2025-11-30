package br.com.utfpr.gerenciamento.server.repository.projection;

import java.math.BigDecimal;

/**
 * Projeção JPA para listagem paginada de Itens.
 *
 * <p>Esta interface otimiza o endpoint /item/page incluindo apenas campos essenciais para exibição
 * em tabelas.
 *
 * <p><b>Campos incluídos:</b>
 *
 * <ul>
 *   <li>id - Identificador único do item
 *   <li>nome - Nome/descrição do item
 *   <li>localizacao - Localização física do item
 *   <li>saldo - Quantidade em estoque
 *   <li>grupo - Grupo/categoria do item (apenas id e descrição)
 *   <li>imagemUrl - URL da primeira imagem do item
 * </ul>
 */
public interface ItemListProjection {

  /** Identificador único do item. */
  Long getId();

  /** Nome/descrição do item. */
  String getNome();

  /** Localização física do item no laboratório. */
  String getLocalizacao();

  /** Quantidade disponível em estoque. */
  BigDecimal getSaldo();

  /** Identificador do grupo/categoria. */
  Long getGrupoId();

  /** Descrição do grupo/categoria. */
  String getGrupoDescricao();

  /** URL da primeira imagem do item. Pode ser null se não houver imagens. */
  String getImagemUrl();
}
