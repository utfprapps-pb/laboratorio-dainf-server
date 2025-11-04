package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.exception.EmailException;
import br.com.utfpr.gerenciamento.server.model.Email;
import br.com.utfpr.gerenciamento.server.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

  @Value("${spring.mail.username}")
  private String emailAddress;

  private final JavaMailSender javaMailSender;
  private final Configuration freemarkerConfiguration;
  private final SpringTemplateEngine thymeleafTemplateEngine;

  public EmailServiceImpl(
      JavaMailSender javaMailSender,
      Configuration freemarkerConfiguration,
      SpringTemplateEngine thymeleafTemplateEngine) {
    this.javaMailSender = javaMailSender;
    this.freemarkerConfiguration = freemarkerConfiguration;
    this.thymeleafTemplateEngine = thymeleafTemplateEngine;
  }

  @Override
  public void enviar(Email email) throws Exception {
    try {
      MimeMessage message = javaMailSender.createMimeMessage();

      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(email.getDe(), "Laboratório de Informática - UTFPR/PB");
      helper.setReplyTo(email.getDe());

      if (email.getPara() != null && !email.getPara().isEmpty()) {
        helper.setBcc(email.getPara());
      } else if (email.getParaList() != null && !email.getParaList().isEmpty()) {
        helper.setBcc(email.getParaList().toArray(new String[0]));
      } else {
        throw new IllegalArgumentException("Nenhum email encontrado para envio.");
      }

      helper.setSubject(email.getTitulo());
      String conteudo = email.getConteudo() != null ? email.getConteudo() : "";
      helper.setText(conteudo, true);

      if (email.getFileMap() != null && !email.getFileMap().isEmpty()) {
        for (Map.Entry<String, byte[]> entry : email.getFileMap().entrySet()) {
          helper.addAttachment(entry.getKey(), new ByteArrayResource(entry.getValue()));
        }
      }

      log.info("Enviando email....");
      javaMailSender.send(message);
      log.info("Email enviado com sucesso.");

    } catch (Exception ex) {
      log.error("Erro ao enviar email: {}", ex.getMessage(), ex);
      throw ex;
    }
  }

  @Override
  public String buildTemplateEmail(Object object, String nameTemplate) {
    Template template;
    try {
      template = freemarkerConfiguration.getTemplate(String.format("%s.ftl", nameTemplate));
      return FreeMarkerTemplateUtils.processTemplateIntoString(template, object);
    } catch (Exception ex) {
      log.error("Erro ao construir template de email. ", ex);
      return null;
    }
  }

  @Override
  public void sendEmailWithTemplate(
      Object objectTemplate, String to, String titleEmail, String nameTemplate) {
    String conteudo;
    if (nameTemplate.endsWith(".html")) {
      // Thymeleaf: remove a extensão para o processador
      String templateName = nameTemplate.substring(0, nameTemplate.length() - 5);
      Map variables;
      if (objectTemplate instanceof Map) {
        variables = (Map<String, Object>) objectTemplate;
      } else {
        try {
          ObjectMapper mapper = new ObjectMapper();
          variables = mapper.convertValue(objectTemplate, Map.class);
        } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException(
              "objectTemplate não pode ser convertido para Map<String,Object> para template Thymeleaf: "
                  + templateName,
              e);
        }
      }
      conteudo = this.buildThymeleafTemplateEmail(variables, templateName);
    } else if (nameTemplate.endsWith(".ftl")) {
      // Freemarker: remove a extensão para o processador
      String templateName = nameTemplate.substring(0, nameTemplate.length() - 4);
      conteudo = this.buildTemplateEmail(objectTemplate, templateName);
    } else {
      // Padrão: tenta Freemarker
      conteudo = this.buildTemplateEmail(objectTemplate, nameTemplate);
    }
    if (conteudo == null || conteudo.trim().isEmpty()) {
      throw new IllegalStateException(
          "Erro ao gerar o conteúdo do e-mail a partir do template '" + nameTemplate + "'.");
    }
    Email email =
        Email.builder().para(to).de(emailAddress).titulo(titleEmail).conteudo(conteudo).build();
    try {
      this.enviar(email);
    } catch (Exception ex) {
      log.error("Erro ao enviar email para {}: {}", to, ex.getMessage(), ex);
      throw new EmailException("Falha ao enviar email", ex);
    }
  }

  public String buildThymeleafTemplateEmail(Map<String, Object> variables, String templateName) {
    Context context = new Context();
    if (variables != null) {
      context.setVariables(variables);
    }
    try {
      return thymeleafTemplateEngine.process(templateName, context);
    } catch (Exception ex) {
      log.error("Erro ao processar template Thymeleaf: {}", templateName, ex);
      return null;
    }
  }
}
