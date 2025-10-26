package br.com.utfpr.gerenciamento.server.dto;

import br.com.utfpr.gerenciamento.server.enumeration.TipoItem;
import br.com.utfpr.gerenciamento.server.model.ItemImage;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import lombok.Data;

/**
 * DTO de resposta para entidade Item.
 *
 * <p>Convertido de record para @Data class para compatibilidade com ModelMapper 3.2.2. ModelMapper
 * com configuração padrão não consegue instanciar records (sem no-arg constructor).
 *
 * <p>Mantém a mesma estrutura JSON para compatibilidade com frontend.
 */
@Data
public class ItemResponseDto {
  private Long id;
  private String nome;
  private BigInteger patrimonio;
  private BigDecimal qtdeMinima;
  private String localizacao;
  private TipoItem tipoItem;
  private BigDecimal saldo;
  private BigDecimal valor;
  private GrupoResponseDto grupo;
  private String descricao;
  private List<ItemImage> imageItem;
  private BigDecimal quantidadeEmprestada;
  private BigDecimal disponivelEmprestimoCalculado;
}
