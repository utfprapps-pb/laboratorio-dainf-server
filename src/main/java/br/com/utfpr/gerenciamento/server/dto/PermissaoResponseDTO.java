package br.com.utfpr.gerenciamento.server.dto;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class PermissaoResponseDTO {
    private Long id;

    private String nome;
}
