package br.com.utfpr.gerenciamento.server.dto;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompraResponseDTO {
  private Long id;

  private LocalDate dataCompra;

  private FornecedorResponseDto fornecedor;

  private UsuarioResponseDto usuario;

  private List<CompraItemResponseDTO> compraItem;
}
