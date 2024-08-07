package br.com.utfpr.gerenciamento.server.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecoverPasswordRequestDto {

    private String code;

    @NotNull
    @Size(min = 6, max = 20)
    // @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$")
    private String password;

    @NotNull
    @Size(min = 6, max = 20)
    // @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$")
    private String repeatPassword;

}
