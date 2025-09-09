package br.com.utfpr.gerenciamento.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

@Data
public class ReservaItemResponseDto {
    private Long id;

    private BigDecimal qtde;

    private ItemResponseDto item;
}
