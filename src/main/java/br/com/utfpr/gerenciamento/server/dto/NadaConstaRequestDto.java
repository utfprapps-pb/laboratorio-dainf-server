package br.com.utfpr.gerenciamento.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NadaConstaRequestDto {
  @NotBlank(message = "O documento não pode estar em branco") private String documento;
}
