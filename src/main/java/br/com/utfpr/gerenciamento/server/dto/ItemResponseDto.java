package br.com.utfpr.gerenciamento.server.dto;

import br.com.utfpr.gerenciamento.server.enumeration.TipoItem;
import br.com.utfpr.gerenciamento.server.model.ItemImage;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public record ItemResponseDto(
    Long id,
    String nome,
    BigInteger patrimonio,
    BigDecimal qtdeMinima,
    String localizacao,
    TipoItem tipoItem,
    BigDecimal saldo,
    BigDecimal valor,
    GrupoResponseDto grupo,
    String descricao,
    List<ItemImage> imageItem,
    BigDecimal quantidadeEmprestada,
    BigDecimal disponivelEmprestimoCalculado) {}
