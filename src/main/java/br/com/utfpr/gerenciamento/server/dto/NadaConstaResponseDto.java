package br.com.utfpr.gerenciamento.server.dto;

import java.time.LocalDateTime;

import br.com.utfpr.gerenciamento.server.enumeration.NadaConstaStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NadaConstaResponseDto {
    private Long id;
    private String usuarioUsername;
    private NadaConstaStatus status;
    private LocalDateTime sendAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
