package br.com.utfpr.gerenciamento.server.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailDto {
    @Email
    private String emailTo;

    private String usuario;

    private String url;

    @NotNull
    private String subject;

    @NotNull
    private String subjectBody;

    @NotNull
    private String contentBody;
}
