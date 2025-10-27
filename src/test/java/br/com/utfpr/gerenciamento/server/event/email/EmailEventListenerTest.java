package br.com.utfpr.gerenciamento.server.event.email;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.event.emprestimo.*;
import br.com.utfpr.gerenciamento.server.event.item.EstoqueMinNotificacaoEvent;
import br.com.utfpr.gerenciamento.server.event.nadaConsta.*;
import br.com.utfpr.gerenciamento.server.mapper.EmprestimoTemplateMapper;
import br.com.utfpr.gerenciamento.server.model.Email;
import br.com.utfpr.gerenciamento.server.model.Emprestimo;
import br.com.utfpr.gerenciamento.server.repository.EmprestimoRepository;
import br.com.utfpr.gerenciamento.server.service.EmailService;
import br.com.utfpr.gerenciamento.server.service.RelatorioService;
import java.lang.reflect.Field;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

  @Test
  void testHandleEmprestimoFinalizadoEventSuccess() {
    EmprestimoFinalizadoEvent event = mock(EmprestimoFinalizadoEvent.class);
    when(event.getEmprestimoId()).thenReturn(1L);
    when(event.getRecipient()).thenReturn("to@email.com");
    when(event.getSubject()).thenReturn("subject");
    when(event.getTemplateName()).thenReturn("template");
    Emprestimo emp = mock(Emprestimo.class);
    when(emprestimoRepository.findEmprestimoByIdWithRelations(1L)).thenReturn(Optional.of(emp));
    Map<String, Object> templateData = new HashMap<>();
    when(templateMapper.toTemplateData(emp)).thenReturn(templateData);
    doNothing()
        .when(emailService)
        .sendEmailWithTemplate(templateData, "to@email.com", "subject", "template");
    listener.handleEmailEvent(event);
    verify(emailService).sendEmailWithTemplate(templateData, "to@email.com", "subject", "template");
  }

  @Test
  void testHandleEmprestimoFinalizadoEventEntityNotFound() {
    EmprestimoFinalizadoEvent event = mock(EmprestimoFinalizadoEvent.class);
    when(event.getEmprestimoId()).thenReturn(2L);
    when(event.getRecipient()).thenReturn("to@email.com");
    when(event.getSubject()).thenReturn("subject");
    when(event.getTemplateName()).thenReturn("template");
    when(emprestimoRepository.findEmprestimoByIdWithRelations(2L)).thenReturn(Optional.empty());
    listener.handleEmailEvent(event);
    verify(emailService, never()).sendEmailWithTemplate(any(), any(), any(), any());
  }

  @Test
  void testHandleEmprestimoFinalizadoEventMailException() {
    EmprestimoFinalizadoEvent event = mock(EmprestimoFinalizadoEvent.class);
    when(event.getEmprestimoId()).thenReturn(3L);
    when(event.getRecipient()).thenReturn("to@email.com");
    when(event.getSubject()).thenReturn("subject");
    when(event.getTemplateName()).thenReturn("template");
    Emprestimo emp = mock(Emprestimo.class);
    when(emprestimoRepository.findEmprestimoByIdWithRelations(3L)).thenReturn(Optional.of(emp));
    Map<String, Object> templateData = new HashMap<>();
    when(templateMapper.toTemplateData(emp)).thenReturn(templateData);
    doThrow(new MailException("fail") {})
        .when(emailService)
        .sendEmailWithTemplate(templateData, "to@email.com", "subject", "template");
    assertThrows(MailException.class, () -> listener.handleEmailEvent(event));
  }

  @Test
  void testHandleEmprestimoFinalizadoEventIllegalArgumentException() {
    EmailEvent event = mock(EmailEvent.class);
    when(event.getRecipient()).thenReturn("to@email.com");
    when(event.getSubject()).thenReturn("subject");
    when(event.getTemplateName()).thenReturn("template");
    assertDoesNotThrow(() -> listener.handleEmailEvent(event));
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
  void testHandleEstoqueMinNotificacaoEventMailException() throws Exception {
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
      doThrow(new MailException("fail") {}).when(emailService).enviar(any(Email.class));
      assertThrows(MailException.class, () -> listener.handleEstoqueMinNotificacaoEvent(event));
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
            templateData, "to@email.com", "Declaração Nada Consta", "nada-consta-declaracao.html");
    listener.handleNadaConstaEmitidoEvent(event);
    verify(emailService)
        .sendEmailWithTemplate(
            templateData, "to@email.com", "Declaração Nada Consta", "nada-consta-declaracao.html");
  }

  @Test
  void testHandleNadaConstaEmitidoEventMailException() {
    Map<String, Object> templateData = new HashMap<>();
    NadaConstaEmitidoEvent event = new NadaConstaEmitidoEvent(this, "to@email.com", templateData);
    doThrow(new MailException("fail") {})
        .when(emailService)
        .sendEmailWithTemplate(
            templateData, "to@email.com", "Declaração Nada Consta", "nada-consta-declaracao.html");
    assertThrows(MailException.class, () -> listener.handleNadaConstaEmitidoEvent(event));
  }

  @Test
  void testHandleNadaConstaEmitidoEventException() {
    Map<String, Object> templateData = new HashMap<>();
    NadaConstaEmitidoEvent event = new NadaConstaEmitidoEvent(this, "to@email.com", templateData);
    doThrow(new RuntimeException("fail"))
        .when(emailService)
        .sendEmailWithTemplate(
            templateData, "to@email.com", "Declaração Nada Consta", "nada-consta-declaracao.html");
    assertThrows(RuntimeException.class, () -> listener.handleNadaConstaEmitidoEvent(event));
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
    listener.handleNadaConstaPendenciasEvent(event);
    verify(emailService)
        .sendEmailWithTemplate(
            templateData,
            "to@email.com",
            "Pendências de Empréstimos",
            "pendencias-emprestimos.html");
  }

  @Test
  void testHandleNadaConstaPendenciasEventMailException() {
    Map<String, Object> templateData = new HashMap<>();
    NadaConstaPendenciasEvent event =
        new NadaConstaPendenciasEvent(this, "to@email.com", templateData);
    doThrow(new MailException("fail") {})
        .when(emailService)
        .sendEmailWithTemplate(
            templateData,
            "to@email.com",
            "Pendências de Empréstimos",
            "pendencias-emprestimos.html");
    assertThrows(MailException.class, () -> listener.handleNadaConstaPendenciasEvent(event));
  }

  @Test
  void testHandleNadaConstaPendenciasEventException() {
    Map<String, Object> templateData = new HashMap<>();
    NadaConstaPendenciasEvent event =
        new NadaConstaPendenciasEvent(this, "to@email.com", templateData);
    doThrow(new RuntimeException("fail"))
        .when(emailService)
        .sendEmailWithTemplate(
            templateData,
            "to@email.com",
            "Pendências de Empréstimos",
            "pendencias-emprestimos.html");
    assertThrows(RuntimeException.class, () -> listener.handleNadaConstaPendenciasEvent(event));
  }
}
