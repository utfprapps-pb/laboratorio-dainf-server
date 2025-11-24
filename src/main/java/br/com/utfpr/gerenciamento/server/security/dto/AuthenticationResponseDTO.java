package br.com.utfpr.gerenciamento.server.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponseDTO {

  private String token;

  private String username;

  private String name;

  private String email;
}
