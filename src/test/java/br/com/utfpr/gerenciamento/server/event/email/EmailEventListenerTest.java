package br.com.utfpr.gerenciamento.server.event.email;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.event.emprestimo.*;
import br.com.utfpr.gerenciamento.server.event.item.EstoqueMinNotificacaoEvent;
import br.com.utfpr.gerenciamento.server.event.nadaConsta.*;
import br.com.utfpr.gerenciamento.server.exception.EntityNotFoundException;
import br.com.utfpr.gerenciamento.server.mapper.EmprestimoTemplateMapper;
import br.com.utfpr.gerenciamento.server.model.Email;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.repository.EmprestimoRepository;
import br.com.utfpr.gerenciamento.server.service.EmailService;
import br.com.utfpr.gerenciamento.server.service.RelatorioService;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.springframework.mail.MailException;

class EmailEventListenerTest {
  @InjectMocks EmailEventListener listener;
  @Mock EmailService emailService;
  @Mock EmprestimoRepository emprestimoRepository;
  @Mock EmprestimoTemplateMapper templateMapper;
  @Mock RelatorioService relatorioService;
  AutoCloseable mocks;

  @BeforeEach
  void setup() throws Exception {
    mocks = MockitoAnnotations.openMocks(this);
    Field field = EmailEventListener.class.getDeclaredField("emailFrom");
    field.setAccessible(true);
    field.set(listener, "from@email.com");
  }

  static List<Object[]> provideEmprestimoFinalizadoEventParams() {
    Emprestimo emp = mock(Emprestimo.class);
    Map<String, Object> templateData = new HashMap<>();
    return List.of(
        new Object[] {1L, "to@email.com", "subject", "template", emp, templateData, true},
        new Object[] {2L, "to@email.com", "subject", "template", null, null, false});
  }

  @ParameterizedTest
  @MethodSource("provideEmprestimoFinalizadoEventParams")
  void testHandleEmprestimoFinalizadoEventParametrized(
      Long id,
      String recipient,
      String subject,
      String template,
      Emprestimo emp,
      Map<String, Object> templateData,
      boolean shouldSend) {
    EmprestimoFinalizadoEvent event = mock(EmprestimoFinalizadoEvent.class);
    when(event.getEmprestimoId()).thenReturn(id);
    when(event.getRecipient()).thenReturn(recipient);
    when(event.getSubject()).thenReturn(subject);
    when(event.getTemplateName()).thenReturn(template);
    if (emp != null) {
      when(emprestimoRepository.findEmprestimoByIdWithRelations(id)).thenReturn(Optional.of(emp));
      when(templateMapper.toTemplateData(emp)).thenReturn(templateData);
      doNothing()
          .when(emailService)
          .sendEmailWithTemplate(templateData, recipient, subject, template);
    } else {
      when(emprestimoRepository.findEmprestimoByIdWithRelations(id)).thenReturn(Optional.empty());
    }
    listener.handleEmailEvent(event);
    if (shouldSend) {
      verify(emailService).sendEmailWithTemplate(templateData, recipient, subject, template);
    } else {
      verify(emailService, never()).sendEmailWithTemplate(any(), any(), any(), any());
    }
  }

  @Test
  void testHandleEstoqueMinNotificacaoEventSuccess() throws Exception {
    EstoqueMinNotificacaoEvent event = mock(EstoqueMinNotificacaoEvent.class);
    when(event.getRecipient()).thenReturn("to@email.com");
    when(event.getSubject()).thenReturn("subject");
    when(event.getTemplateName()).thenReturn("template");
    byte[] pdf = new byte[] {1, 2, 3};
    when(relatorioService.generateReport(anyLong(), isNull())).thenReturn(null);
    try (MockedStatic<net.sf.jasperreports.engine.JasperExportManager> jasperMock =
        mockStatic(net.sf.jasperreports.engine.JasperExportManager.class)) {
      jasperMock
          .when(() -> net.sf.jasperreports.engine.JasperExportManager.exportReportToPdf(null))
          .thenReturn(pdf);
      when(emailService.buildTemplateEmail(null, "template")).thenReturn("conteudo");
      doNothing().when(emailService).enviar(any(Email.class));
      listener.handleEstoqueMinNotificacaoEvent(event);
      verify(emailService).enviar(any(Email.class));
    }
  }

  @Test
  void testHandleEstoqueMinNotificacaoEventException() throws Exception {
    EstoqueMinNotificacaoEvent event = mock(EstoqueMinNotificacaoEvent.class);
    when(event.getRecipient()).thenReturn("to@email.com");
    when(event.getSubject()).thenReturn("subject");
    when(event.getTemplateName()).thenReturn("template");
    when(relatorioService.generateReport(anyLong(), isNull()))
        .thenThrow(new RuntimeException("fail"));
    listener.handleEstoqueMinNotificacaoEvent(event);
  }

  @Test
  void testHandleNadaConstaEmitidoEventSuccess() {
    Map<String, Object> templateData = new HashMap<>();
    NadaConstaEmitidoEvent event = new NadaConstaEmitidoEvent(this, "to@email.com", templateData);
    doNothing()
        .when(emailService)
        .sendEmailWithTemplate(
            templateData,
            "to@email.com",
            "Declaração Nada Consta",
            "nada-consta-declaracao.html"); // 4 args
    listener.handleEmailEvent(event);
    verify(emailService)
        .sendEmailWithTemplate(
            templateData,
            "to@email.com",
            "Declaração Nada Consta",
            "nada-consta-declaracao.html"); // 4 args
  }

  @Test
  void testHandleNadaConstaEmitidoEventComCC() {
    Map<String, Object> templateData = new HashMap<>();
    String cc = "cc@email.com";
    NadaConstaEmitidoEvent event = new NadaConstaEmitidoEvent(this, "to@email.com", templateData, cc);
    doNothing()
        .when(emailService)
        .sendEmailWithTemplate(
            templateData,
            "to@email.com",
            "Declaração Nada Consta",
            "nada-consta-declaracao.html",
            cc);
    listener.handleEmailEvent(event);
    verify(emailService)
        .sendEmailWithTemplate(
            templateData,
            "to@email.com",
            "Declaração Nada Consta",
            "nada-consta-declaracao.html",
            cc);
  }

  @Test
  void testHandleNadaConstaPendenciasEventSuccess() {
    Map<String, Object> templateData = new HashMap<>();
    NadaConstaPendenciasEvent event =
        new NadaConstaPendenciasEvent(this, "to@email.com", templateData);
    doNothing()
        .when(emailService)
        .sendEmailWithTemplate(
            templateData,
            "to@email.com",
            "Pendências de Empréstimos",
            "pendencias-emprestimos.html");
    listener.handleEmailEvent(event);
    verify(emailService)
        .sendEmailWithTemplate(
            templateData,
            "to@email.com",
            "Pendências de Empréstimos",
            "pendencias-emprestimos.html");
  }

  @Test
  void testProcessEmailWithTemplateSuccess() throws Exception {
    doNothing()
        .when(emailService)
        .sendEmailWithTemplate("data", "to@email.com", "subject", "template");
    Method m =
        EmailEventListener.class.getDeclaredMethod(
            "processEmailWithTemplate",
            Object.class,
            String.class,
            String.class,
            String.class,
            String.class);
    m.setAccessible(true);
    m.invoke(listener, "data", "to@email.com", "subject", "template", null);
    verify(emailService).sendEmailWithTemplate("data", "to@email.com", "subject", "template");
  }

  @Test
  void testProcessEmailWithTemplateMailException() throws Exception {
    doThrow(new MailException("fail") {})
        .when(emailService)
        .sendEmailWithTemplate(any(), any(), any(), any());
    Method m =
        EmailEventListener.class.getDeclaredMethod(
            "processEmailWithTemplate",
            Object.class,
            String.class,
            String.class,
            String.class,
            String.class);
    m.setAccessible(true);
    assertThrows(
        MailException.class,
        () -> {
          try {
            m.invoke(listener, "data", "to@email.com", "subject", "template", null);
          } catch (Exception ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof MailException e) throw e;
            throw new RuntimeException(ex);
          }
        });
  }

  @Test
  void testProcessEmailWithTemplateEntityNotFoundException() throws Exception {
    doThrow(new EntityNotFoundException("fail"))
        .when(emailService)
        .sendEmailWithTemplate(any(), any(), any(), any());
    Method m =
        EmailEventListener.class.getDeclaredMethod(
            "processEmailWithTemplate",
            Object.class,
            String.class,
            String.class,
            String.class,
            String.class);
    m.setAccessible(true);
    m.invoke(listener, "data", "to@email.com", "subject", "template", null);
  }

  @Test
  void testProcessEmailWithTemplateIllegalArgumentException() throws Exception {
    doThrow(new IllegalArgumentException("fail"))
        .when(emailService)
        .sendEmailWithTemplate(any(), any(), any(), any());
    Method m =
        EmailEventListener.class.getDeclaredMethod(
            "processEmailWithTemplate",
            Object.class,
            String.class,
            String.class,
            String.class,
            String.class);
    m.setAccessible(true);
    m.invoke(listener, "data", "to@email.com", "subject", "template", null);
  }

  @Test
  void testProcessEmailWithAttachmentSuccess() throws Exception {
    Email email =
        Email.builder()
            .para("to@email.com")
            .de("from@email.com")
            .titulo("subject")
            .conteudo("body")
            .build();
    doNothing().when(emailService).enviar(email);
    Method m =
        EmailEventListener.class.getDeclaredMethod(
            "processEmailWithAttachment", Email.class, String.class, String.class);
    m.setAccessible(true);
    m.invoke(listener, email, "to@email.com", "subject");
    verify(emailService).enviar(email);
  }

  @Test
  void testProcessEmailWithAttachmentMailException() throws Exception {
    Email email =
        Email.builder()
            .para("to@email.com")
            .de("from@email.com")
            .titulo("subject")
            .conteudo("body")
            .build();
    doThrow(new MailException("fail") {}).when(emailService).enviar(email);
    Method m =
        EmailEventListener.class.getDeclaredMethod(
            "processEmailWithAttachment", Email.class, String.class, String.class);
    m.setAccessible(true);
    assertThrows(
        MailException.class,
        () -> {
          try {
            m.invoke(listener, email, "to@email.com", "subject");
          } catch (Exception ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof MailException e) throw e;
            throw new RuntimeException(ex);
          }
        });
  }

  @Test
  void testProcessEmailWithAttachmentGenericException() throws Exception {
    Email email =
        Email.builder()
            .para("to@email.com")
            .de("from@email.com")
            .titulo("subject")
            .conteudo("body")
            .build();
    doThrow(new RuntimeException("fail")).when(emailService).enviar(email);
    Method m =
        EmailEventListener.class.getDeclaredMethod(
            "processEmailWithAttachment", Email.class, String.class, String.class);
    m.setAccessible(true);
    m.invoke(listener, email, "to@email.com", "subject");
  }

  @Test
  void testPrepareTemplateDataForEventUnsupportedType() throws Exception {
    EmailEvent event = mock(EmailEvent.class);
    Method m =
        EmailEventListener.class.getDeclaredMethod("prepareTemplateDataForEvent", EmailEvent.class);
    m.setAccessible(true);
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          try {
            m.invoke(listener, event);
          } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException ex) throw ex;
            throw new RuntimeException(e);
          }
        });
  }

  @SuppressWarnings("unchecked")
  @Test
  void testPrepareEmprestimoTemplateDataSuccess() throws Exception {
    Emprestimo emp = mock(Emprestimo.class);
    when(emprestimoRepository.findEmprestimoByIdWithRelations(10L)).thenReturn(Optional.of(emp));
    Map<String, Object> templateData = new HashMap<>();
    when(templateMapper.toTemplateData(emp)).thenReturn(templateData);
    Method m =
        EmailEventListener.class.getDeclaredMethod("prepareEmprestimoTemplateData", Long.class);
    m.setAccessible(true);
    Map<String, Object> result = (Map<String, Object>) m.invoke(listener, 10L);
    assertEquals(templateData, result);
  }

  @Test
  void testPrepareEmprestimoTemplateDataEntityNotFound() throws Exception {
    when(emprestimoRepository.findEmprestimoByIdWithRelations(11L)).thenReturn(Optional.empty());
    Method m =
        EmailEventListener.class.getDeclaredMethod("prepareEmprestimoTemplateData", Long.class);
    m.setAccessible(true);
    assertThrows(
        EntityNotFoundException.class,
        () -> {
          try {
            m.invoke(listener, 11L);
          } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof EntityNotFoundException ex) throw ex;
            throw new RuntimeException(e);
          }
        });
  }

  @Test
  void testHandleEmprestimoFinalizadoEventComCC() {
    Emprestimo emp = mock(Emprestimo.class);
    when(emprestimoRepository.findEmprestimoByIdWithRelations(10L)).thenReturn(Optional.of(emp));
    Map<String, Object> templateData = new HashMap<>();
    when(templateMapper.toTemplateData(emp)).thenReturn(templateData);
    EmprestimoFinalizadoEvent event =
        new EmprestimoFinalizadoEvent(this, 10L, "to@email.com", true);
    String cc = "cc@email.com";
    // Stub 5-arg overload
    doNothing()
        .when(emailService)
        .sendEmailWithTemplate(
            eq(templateData),
            eq("to@email.com"),
            eq("Confirmação de Empréstimo"),
            eq("templateConfirmacaoEmprestimo.html"),
            eq(cc));
    listener.handleEmailEvent(event);
    // Capture and verify CC argument
    ArgumentCaptor<String> ccCaptor = ArgumentCaptor.forClass(String.class);
    verify(emailService)
        .sendEmailWithTemplate(
            eq(templateData),
            eq("to@email.com"),
            eq("Confirmação de Empréstimo"),
            eq("templateConfirmacaoEmprestimo.html"),
            ccCaptor.capture());
    assertEquals(cc, ccCaptor.getValue());
    // Ensure 4-arg overload is NOT called
    verify(emailService, never())
        .sendEmailWithTemplate(eq(templateData), eq("to@email.com"), anyString(), anyString());
  }

  @Test
  void testHandleEmprestimoFinalizadoEventSemCC() {
    Emprestimo emp = mock(Emprestimo.class);
    when(emprestimoRepository.findEmprestimoByIdWithRelations(11L)).thenReturn(Optional.of(emp));
    Map<String, Object> templateData = new HashMap<>();
    when(templateMapper.toTemplateData(emp)).thenReturn(templateData);
    EmprestimoFinalizadoEvent event =
        new EmprestimoFinalizadoEvent(this, 11L, "to@email.com", false);
    // Stub 4-arg overload
    doNothing()
        .when(emailService)
        .sendEmailWithTemplate(
            eq(templateData),
            eq("to@email.com"),
            eq("Confirmação de Empréstimo"),
            eq("templateConfirmacaoFinalizacaoEmprestimo.html"));
    listener.handleEmailEvent(event);
    // Verify 4-arg overload called
    verify(emailService)
        .sendEmailWithTemplate(
            eq(templateData),
            eq("to@email.com"),
            eq("Confirmação de Empréstimo"),
            eq("templateConfirmacaoFinalizacaoEmprestimo.html"));
    // Ensure 5-arg overload is NOT called
    verify(emailService, never())
        .sendEmailWithTemplate(eq(templateData), eq("to@email.com"), anyString(), anyString(), any());
  }
}
