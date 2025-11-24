package br.com.utfpr.gerenciamento.server.dto;

import br.com.utfpr.gerenciamento.server.config.LocalDateDeserializer;
import br.com.utfpr.gerenciamento.server.config.LocalDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class SolicitacaoResponseDto {
  private Long id;

  private String descricao;

  @JsonSerialize(using = LocalDateSerializer.class)
  @JsonDeserialize(using = LocalDateDeserializer.class)
  private LocalDate dataSolicitacao;

  private UsuarioResponseDto usuario;

  private List<SolicitacaoItemResponseDto> solicitacaoItem;

  private String observacao;
}
