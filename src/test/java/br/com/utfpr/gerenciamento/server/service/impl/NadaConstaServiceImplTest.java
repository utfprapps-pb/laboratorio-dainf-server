package br.com.utfpr.gerenciamento.server.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.enumeration.NadaConstaStatus;
import br.com.utfpr.gerenciamento.server.exception.NadaConstaException;
import br.com.utfpr.gerenciamento.server.model.NadaConsta;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.repository.NadaConstaRepository;
import br.com.utfpr.gerenciamento.server.service.EmprestimoService;
import br.com.utfpr.gerenciamento.server.service.SystemConfigService;
import br.com.utfpr.gerenciamento.server.service.UsuarioService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.thymeleaf.TemplateEngine;

@ExtendWith(MockitoExtension.class)
class NadaConstaServiceImplTest {
  @Mock private NadaConstaRepository nadaConstaRepository;
  @Mock private UsuarioService usuarioService;
  @Mock private ModelMapper modelMapper;
  @Mock private EmprestimoService emprestimoService;
  @Mock private SystemConfigService systemConfigService;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private TemplateEngine templateEngine;

  private NadaConstaServiceImpl nadaConstaService;

  @org.junit.jupiter.api.BeforeEach
  void setUp() {
    nadaConstaService =
        new NadaConstaServiceImpl(
            nadaConstaRepository,
            usuarioService,
            modelMapper,
            emprestimoService,
            systemConfigService,
            eventPublisher,
            templateEngine);
  }

  @Test
  void testGerarNadaConstaPdf_CompletedStatus_ReturnsPdfBytes() {
    Usuario usuario = Usuario.builder().nome("Teste Usuário").documento("123456").build();
    NadaConsta nadaConsta =
        NadaConsta.builder()
            .id(1L)
            .usuario(usuario)
            .status(NadaConstaStatus.COMPLETED)
            .createdAt(LocalDateTime.now())
            .build();
    when(nadaConstaRepository.findById(1L)).thenReturn(java.util.Optional.of(nadaConsta));
    // Mock templateEngine to return a simple HTML string
    when(templateEngine.process(eq("nada-consta-declaracao"), any()))
        .thenReturn("<html><body>PDF</body></html>");
    byte[] pdf = nadaConstaService.gerarNadaConstaPdf(1L);
    assertNotNull(pdf);
    assertTrue(pdf.length > 0);
  }

  @Test
  void testGerarNadaConstaPdf_NotCompleted_ThrowsException() {
    Usuario usuario = Usuario.builder().nome("Teste Usuário").documento("123456").build();
    NadaConsta nadaConsta =
        NadaConsta.builder()
            .id(2L)
            .usuario(usuario)
            .status(NadaConstaStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();
    when(nadaConstaRepository.findById(2L)).thenReturn(java.util.Optional.of(nadaConsta));
    assertThrows(NadaConstaException.class, () -> nadaConstaService.gerarNadaConstaPdf(2L));
  }
}
