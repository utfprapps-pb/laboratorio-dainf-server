package br.com.utfpr.gerenciamento.server.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.model.Email;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.mail.internet.MimeMessage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

class EmailServiceImplTest {

  @Mock private JavaMailSender javaMailSender;
  @Mock private Configuration freemarkerConfiguration;
  @Mock private SpringTemplateEngine thymeleafTemplateEngine;
  @Mock private Template freemarkerTemplate;
  @Mock private MimeMessage mimeMessage;

  @InjectMocks private EmailServiceImpl emailService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    emailService =
        new EmailServiceImpl(javaMailSender, freemarkerConfiguration, thymeleafTemplateEngine);
  }

  @Test
  void testEnviarEmailWithPara() throws Exception {
    when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    doNothing().when(javaMailSender).send(any(MimeMessage.class));
    Email email =
        Email.builder()
            .de("from@test.com")
            .para("to@test.com")
            .titulo("Test Subject")
            .conteudo("Test Body")
            .build();
    emailService.enviar(email);
    verify(javaMailSender, timeout(1000)).send(any(MimeMessage.class));
  }

  @Test
  void testEnviarEmailWithParaList() throws Exception {
    when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    doNothing().when(javaMailSender).send(any(MimeMessage.class));
    Email email =
        Email.builder()
            .de("from@test.com")
            .paraList(Collections.singletonList("to@test.com"))
            .titulo("Test Subject")
            .conteudo("Test Body")
            .build();
    emailService.enviar(email);
    verify(javaMailSender, timeout(1000)).send(any(MimeMessage.class));
  }

  @Test
  void testEnviarEmailThrowsExceptionIfNoRecipient() {
    Email email =
        Email.builder().de("from@test.com").titulo("Test Subject").conteudo("Test Body").build();
    assertDoesNotThrow(() -> emailService.enviar(email)); // Exception is caught and logged
  }

  @Test
  void testEnviarEmailWithAttachment() throws Exception {
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
    emailService.enviar(email);
    verify(javaMailSender, timeout(1000)).send(any(MimeMessage.class));
  }

  @Test
  void testBuildTemplateEmailReturnsContent() throws Exception {
    when(freemarkerConfiguration.getTemplate(anyString())).thenReturn(freemarkerTemplate);
    try (var mockedStatic = mockStatic(FreeMarkerTemplateUtils.class)) {
      mockedStatic
          .when(
              () ->
                  FreeMarkerTemplateUtils.processTemplateIntoString(eq(freemarkerTemplate), any()))
          .thenReturn("template content");
      String result = emailService.buildTemplateEmail(Collections.emptyMap(), "templateName");
      assertEquals("template content", result);
    }
  }

  @Test
  void testBuildTemplateEmailReturnsNullOnException() throws Exception {
    when(freemarkerConfiguration.getTemplate(anyString())).thenThrow(new RuntimeException("fail"));
    String result = emailService.buildTemplateEmail(Collections.emptyMap(), "templateName");
    assertNull(result);
  }

  @Test
  void testSendEmailWithTemplateThymeleaf() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("key", "value");
    when(thymeleafTemplateEngine.process(anyString(), any(Context.class)))
        .thenReturn("thymeleaf content");
    doNothing().when(javaMailSender).send(any(MimeMessage.class));
    emailService.sendEmailWithTemplate(variables, "to@test.com", "title", "template.html");
    verify(thymeleafTemplateEngine).process(eq("template"), any(Context.class));
  }

  @Test
  void testSendEmailWithTemplateFreemarker() throws Exception {
    when(freemarkerConfiguration.getTemplate(anyString())).thenReturn(freemarkerTemplate);
    try (var mockedStatic = mockStatic(FreeMarkerTemplateUtils.class)) {
      mockedStatic
          .when(
              () ->
                  FreeMarkerTemplateUtils.processTemplateIntoString(eq(freemarkerTemplate), any()))
          .thenReturn("freemarker content");
      doNothing().when(javaMailSender).send(any(MimeMessage.class));
      emailService.sendEmailWithTemplate(
          Collections.emptyMap(), "to@test.com", "title", "template.ftl");
      verify(freemarkerConfiguration).getTemplate(eq("template.ftl"));
    }
  }

  @Test
  void testSendEmailWithTemplateDefault() throws Exception {
    when(freemarkerConfiguration.getTemplate(anyString())).thenReturn(freemarkerTemplate);
    try (var mockedStatic = mockStatic(FreeMarkerTemplateUtils.class)) {
      mockedStatic
          .when(
              () ->
                  FreeMarkerTemplateUtils.processTemplateIntoString(eq(freemarkerTemplate), any()))
          .thenReturn("default content");
      doNothing().when(javaMailSender).send(any(MimeMessage.class));
      emailService.sendEmailWithTemplate(
          Collections.emptyMap(), "to@test.com", "title", "template");
      verify(freemarkerConfiguration).getTemplate(eq("template.ftl"));
    }
  }

  @Test
  void testSendEmailWithTemplateEmptyContent() throws Exception {
    when(freemarkerConfiguration.getTemplate(anyString())).thenReturn(freemarkerTemplate);
    try (var mockedStatic = mockStatic(FreeMarkerTemplateUtils.class)) {
      mockedStatic
          .when(
              () ->
                  FreeMarkerTemplateUtils.processTemplateIntoString(eq(freemarkerTemplate), any()))
          .thenReturn("");
      emailService.sendEmailWithTemplate(
          Collections.emptyMap(), "to@test.com", "title", "template.ftl");
      verify(javaMailSender, never()).send(any(MimeMessage.class));
    }
  }

  @Test
  void testBuildThymeleafTemplateEmailReturnsContent() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("key", "value");
    when(thymeleafTemplateEngine.process(anyString(), any(Context.class)))
        .thenReturn("thymeleaf result");
    String result = emailService.buildThymeleafTemplateEmail(variables, "templateName");
    assertEquals("thymeleaf result", result);
  }

  @Test
  void testBuildThymeleafTemplateEmailReturnsNullOnException() {
    Map<String, Object> variables = new HashMap<>();
    when(thymeleafTemplateEngine.process(anyString(), any(Context.class)))
        .thenThrow(new RuntimeException("fail"));
    String result = emailService.buildThymeleafTemplateEmail(variables, "templateName");
    assertNull(result);
  }
}
