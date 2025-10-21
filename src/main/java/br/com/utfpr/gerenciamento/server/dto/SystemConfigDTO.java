package br.com.utfpr.gerenciamento.server.dto;

import br.com.utfpr.gerenciamento.server.annotation.UtfprEmailValidator;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SystemConfigDTO {
  @NotNull @UtfprEmailValidator private String nadaConstaEmail;
}
