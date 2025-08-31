package br.com.utfpr.gerenciamento.server.dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConfirmEmailRequestDto {

  private String code;

  private String email;
}
