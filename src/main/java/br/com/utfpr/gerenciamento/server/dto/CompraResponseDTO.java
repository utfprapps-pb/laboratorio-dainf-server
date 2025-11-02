package br.com.utfpr.gerenciamento.server.dto;

import br.com.utfpr.gerenciamento.server.config.LocalDateDeserializer;
import br.com.utfpr.gerenciamento.server.config.LocalDateSerializer;
import br.com.utfpr.gerenciamento.server.model.CompraItem;
import br.com.utfpr.gerenciamento.server.model.Fornecedor;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

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
