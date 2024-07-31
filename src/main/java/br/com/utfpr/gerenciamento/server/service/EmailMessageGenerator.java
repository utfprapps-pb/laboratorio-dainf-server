package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.dto.EmailDto;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailMessageGenerator {

    private final TemplateEngine templateEngine;

    public EmailMessageGenerator(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String generateHTML(EmailDto emailDto, String template) {
        Context context = new Context();
        context.setVariables(DataToMap(emailDto));

        return templateEngine.process(template, context);
    }

    public Map<String, Object> DataToMap(EmailDto emailDto) {
        Map<String, Object> map = new HashMap<>();
        map.put("subject", emailDto.getSubject());
        map.put("content", emailDto.getContentBody());
        map.put("url", emailDto.getUrl());
        map.put("usuario", emailDto.getUsuario());

        return map;
    }
}
