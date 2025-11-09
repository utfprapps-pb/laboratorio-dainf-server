package br.com.utfpr.gerenciamento.server.dto;

import java.time.LocalDate;
import java.util.List;

import br.com.utfpr.gerenciamento.server.config.LocalDateDeserializer;
import br.com.utfpr.gerenciamento.server.config.LocalDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class ReservaResponseDto {
  private Long id;

  private String descricao;
  @JsonSerialize(using = LocalDateSerializer.class)
  @JsonDeserialize(using = LocalDateDeserializer.class)
  private LocalDate dataReserva;
  @JsonSerialize(using = LocalDateSerializer.class)
  @JsonDeserialize(using = LocalDateDeserializer.class)
  private LocalDate dataRetirada;

  private String observacao;

  private UsuarioResponseDto usuario;

  private List<ReservaItemResponseDto> reservaItem;
}
