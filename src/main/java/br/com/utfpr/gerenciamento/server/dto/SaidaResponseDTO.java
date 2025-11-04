package br.com.utfpr.gerenciamento.server.dto;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaidaResponseDTO {
  private Long id;

  private LocalDate dataSaida;

  private String observacao;

  private List<SaidaItemResponseDTO> saidaItem;

  private UsuarioResponseDto usuarioResponsavel;

  private Long idEmprestimo;
}
