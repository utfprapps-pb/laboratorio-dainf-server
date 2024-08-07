package br.com.utfpr.gerenciamento.server.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

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

    private Map<String, Object> body;
}
