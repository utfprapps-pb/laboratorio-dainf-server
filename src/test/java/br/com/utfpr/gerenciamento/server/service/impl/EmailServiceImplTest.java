package br.com.utfpr.gerenciamento.server.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.exception.EmailException;
import br.com.utfpr.gerenciamento.server.model.Email;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.mail.internet.MimeMessage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

  @Mock private JavaMailSender javaMailSender;
  @Mock private Configuration freemarkerConfiguration;
  @Mock private SpringTemplateEngine thymeleafTemplateEngine;
  @Mock private Template freemarkerTemplate;
  @Mock private MimeMessage mimeMessage;

  @InjectMocks private EmailServiceImpl emailService;

  @BeforeEach
  void setUp() {
    // Injeta emailAddress via ReflectionTestUtils (campo privado anotado com @Value)
    ReflectionTestUtils.setField(emailService, "emailAddress", "test@utfpr.edu.br");
  }

  @Test
  void enviar_DeveEnviarEmailComDestinatarioUnico() throws Exception {
    // Given
    when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    doNothing().when(javaMailSender).send(any(MimeMessage.class));

    Email email =
        Email.builder()
            .de("from@test.com")
            .para("to@test.com")
            .titulo("Test Subject")
            .conteudo("Test Body")
            .build();

    // When
    emailService.enviar(email);

    // Then
    verify(javaMailSender).send(any(MimeMessage.class));
  }

  @Test
  void enviar_DeveEnviarEmailComListaDeDestinatarios() throws Exception {
    // Given
    when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    doNothing().when(javaMailSender).send(any(MimeMessage.class));

    Email email =
        Email.builder()
            .de("from@test.com")
            .paraList(Collections.singletonList("to@test.com"))
            .titulo("Test Subject")
            .conteudo("Test Body")
            .build();

    // When
    emailService.enviar(email);

    // Then
    verify(javaMailSender).send(any(MimeMessage.class));
  }

  @Test
  void enviar_DeveLancarExcecaoQuandoNaoHaDestinatario() {
    // Given
    when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

    Email email =
        Email.builder().de("from@test.com").titulo("Test Subject").conteudo("Test Body").build();

    // When/Then
    assertThrows(
        IllegalArgumentException.class,
        () -> emailService.enviar(email),
        "Nenhum email encontrado para envio.");
  }

  @Test
  void enviar_DeveEnviarEmailComAnexo() throws Exception {
    // Given
    when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    doNothing().when(javaMailSender).send(any(MimeMessage.class));

    Map<String, byte[]> fileMap = new HashMap<>();
    fileMap.put("file.txt", "test content".getBytes());

    Email email =
        Email.builder()
            .de("from@test.com")
            .para("to@test.com")
            .titulo("Test Subject")
            .conteudo("Test Body")
            .fileMap(fileMap)
            .build();

    // When
    emailService.enviar(email);

    // Then
    verify(javaMailSender).send(any(MimeMessage.class));
  }

  @Test
  void enviar_DevePropagarExcecaoQuandoFalhaNoEnvio() {
    // Given
    when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    doThrow(
            new MailException("Falha SMTP") {
              @Override
              public String getMessage() {
                return "Falha SMTP";
              }
            })
        .when(javaMailSender)
        .send(any(MimeMessage.class));

    Email email =
        Email.builder()
            .de("from@test.com")
            .para("to@test.com")
            .titulo("Test Subject")
            .conteudo("Test Body")
            .build();

    // When/Then
    assertThrows(Exception.class, () -> emailService.enviar(email));
  }

  @Test
  void buildTemplateEmail_DeveRetornarConteudoDoTemplate() throws Exception {
    // Given
    when(freemarkerConfiguration.getTemplate(anyString())).thenReturn(freemarkerTemplate);

    try (var mockedStatic = mockStatic(FreeMarkerTemplateUtils.class)) {
      mockedStatic
          .when(
              () ->
                  FreeMarkerTemplateUtils.processTemplateIntoString(eq(freemarkerTemplate), any()))
          .thenReturn("template content");

      // When
      String result = emailService.buildTemplateEmail(Collections.emptyMap(), "templateName");

      // Then
      assertEquals("template content", result);
    }
  }

  @Test
  void buildTemplateEmail_DeveRetornarNullQuandoOcorreExcecao() throws Exception {
    // Given
    when(freemarkerConfiguration.getTemplate(anyString())).thenThrow(new RuntimeException("fail"));

    // When
    String result = emailService.buildTemplateEmail(Collections.emptyMap(), "templateName");

    // Then
    assertNull(result);
  }

  @Test
  void sendEmailWithTemplate_DeveEnviarEmailComTemplateThymeleaf() {
    // Given
    Map<String, Object> variables = new HashMap<>();
    variables.put("key", "value");

    when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    when(thymeleafTemplateEngine.process(anyString(), any(Context.class)))
        .thenReturn("thymeleaf content");
    doNothing().when(javaMailSender).send(any(MimeMessage.class));

    // When
    emailService.sendEmailWithTemplate(variables, "to@test.com", "title", "template.html");

    // Then
    verify(thymeleafTemplateEngine).process(eq("template"), any(Context.class));
  }

  @Test
  void sendEmailWithTemplate_DeveEnviarEmailComTemplateFreemarker() throws Exception {
    // Given
    when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    when(freemarkerConfiguration.getTemplate(anyString())).thenReturn(freemarkerTemplate);

    try (var mockedStatic = mockStatic(FreeMarkerTemplateUtils.class)) {
      mockedStatic
          .when(
              () ->
                  FreeMarkerTemplateUtils.processTemplateIntoString(eq(freemarkerTemplate), any()))
          .thenReturn("freemarker content");
      doNothing().when(javaMailSender).send(any(MimeMessage.class));

      // When
      emailService.sendEmailWithTemplate(
          Collections.emptyMap(), "to@test.com", "title", "template.ftl");

      // Then
      verify(freemarkerConfiguration).getTemplate("template.ftl");
    }
  }

  @Test
  void sendEmailWithTemplate_DeveUsarFreemarkerPorPadrao() throws Exception {
    // Given
    when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    when(freemarkerConfiguration.getTemplate(anyString())).thenReturn(freemarkerTemplate);

    try (var mockedStatic = mockStatic(FreeMarkerTemplateUtils.class)) {
      mockedStatic
          .when(
              () ->
                  FreeMarkerTemplateUtils.processTemplateIntoString(eq(freemarkerTemplate), any()))
          .thenReturn("default content");
      doNothing().when(javaMailSender).send(any(MimeMessage.class));

      // When
      emailService.sendEmailWithTemplate(
          Collections.emptyMap(), "to@test.com", "title", "template");

      // Then
      verify(freemarkerConfiguration).getTemplate("template.ftl");
    }
  }

  @Test
  void sendEmailWithTemplate_DeveLancarExcecaoQuandoConteudoVazio() throws Exception {
    // Given
    when(freemarkerConfiguration.getTemplate(anyString())).thenReturn(freemarkerTemplate);

    try (var mockedStatic = mockStatic(FreeMarkerTemplateUtils.class)) {
      mockedStatic
          .when(
              () ->
                  FreeMarkerTemplateUtils.processTemplateIntoString(eq(freemarkerTemplate), any()))
          .thenReturn("");

      // When/Then
      Map<String, Object> emptyMap = Collections.emptyMap();
      assertThrows(
          IllegalStateException.class,
          () ->
              emailService.sendEmailWithTemplate(emptyMap, "to@test.com", "title", "template.ftl"),
          "Erro ao gerar o conteúdo do e-mail");
    }
  }

  @Test
  void sendEmailWithTemplate_DevePropagarEmailExceptionQuandoFalhaNoEnvio() {
    // Given
    when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    when(thymeleafTemplateEngine.process(anyString(), any(Context.class)))
        .thenReturn("conteudo valido");
    doThrow(
            new MailException("Falha SMTP") {
              @Override
              public String getMessage() {
                return "Falha SMTP";
              }
            })
        .when(javaMailSender)
        .send(any(MimeMessage.class));

    // When/Then
    Map<String, Object> variables = new HashMap<>();
    assertThrows(
        EmailException.class,
        () ->
            emailService.sendEmailWithTemplate(variables, "to@test.com", "titulo", "template.html"),
        "Falha ao enviar email");
  }

  @Test
  void buildThymeleafTemplateEmail_DeveRetornarConteudoDoTemplate() {
    // Given
    Map<String, Object> variables = new HashMap<>();
    variables.put("key", "value");

    when(thymeleafTemplateEngine.process(anyString(), any(Context.class)))
        .thenReturn("thymeleaf result");

    // When
    String result = emailService.buildThymeleafTemplateEmail(variables, "templateName");

    // Then
    assertEquals("thymeleaf result", result);
  }

  @Test
  void buildThymeleafTemplateEmail_DeveRetornarNullQuandoOcorreExcecao() {
    // Given
    Map<String, Object> variables = new HashMap<>();
    when(thymeleafTemplateEngine.process(anyString(), any(Context.class)))
        .thenThrow(new RuntimeException("fail"));

    // When
    String result = emailService.buildThymeleafTemplateEmail(variables, "templateName");

    // Then
    assertNull(result);
  }
}
