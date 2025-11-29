package br.com.utfpr.gerenciamento.server.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.repository.UsuarioRepository;
import br.com.utfpr.gerenciamento.server.service.PermissaoService;
import br.com.utfpr.gerenciamento.server.service.impl.UsuarioServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JWTAuthenticationFilterTest {

  @Mock private AuthenticationManager authenticationManager;
  @Mock private UsuarioServiceImpl usuarioService;
  @Mock private UsuarioRepository usuarioRepository;
  @Mock private Environment env;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private FilterChain chain;
  @Mock private PermissaoService permissaoService;

  private JWTAuthenticationFilter filter;

  @BeforeEach
  void setUp() {
    when(env.getProperty(anyString())).thenReturn("test-secret");
    filter =
        new JWTAuthenticationFilter(authenticationManager, usuarioService, usuarioRepository, env);
  }

  @Test
  void shouldReturn428IfNadaConstaSolicitacaoEmAberto() throws IOException {
    // Criar JSON manualmente para evitar problemas de desserialização
    String json = "{\"username\":\"user@utfpr.edu.br\",\"password\":\"pass\"}";

    Usuario usuario = new Usuario();
    usuario.setUsername("user@utfpr.edu.br");
    usuario.setPassword("pass");
    usuario.setPermissoes(new java.util.HashSet<>());

    when(request.getInputStream()).thenReturn(new DelegatingServletInputStream(json.getBytes()));
    // Mock ambos os métodos do repository usados no fluxo
    when(usuarioRepository.findWithPermissoesByUsernameOrEmail(anyString(), anyString()))
        .thenReturn(usuario);
    when(usuarioRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(usuario);
    when(usuarioService.hasSolicitacaoNadaConstaPendingOrCompleted(anyString())).thenReturn(true);
    assertThrows(
        PreconditionRequiredAuthenticationException.class,
        () -> filter.attemptAuthentication(request, response));
  }

  @Test
  void unsuccessfulAuthenticationShouldReturnJsonAnd428ForNadaConsta() throws IOException {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    PreconditionRequiredAuthenticationException ex =
        new PreconditionRequiredAuthenticationException(
            "Foi realizado uma solicitação de nada consta para o usuário. Contate a administração.");
    filter.unsuccessfulAuthentication(request, response, ex);
    verify(response).setStatus(428);
    verify(response).setContentType("application/problem+json");
    pw.flush();
    String json = sw.toString();
    assertTrue(json.contains("nada consta"));
    assertTrue(json.contains("detail")); // RFC 7807 usa "detail" em vez de "error"
  }

  @Test
  void testAttemptAuthentication_Success() throws Exception {
    // Create JSON directly instead of serializing Usuario object
    String json = "{\"username\":\"user@utfpr.edu.br\",\"password\":\"password\"}";
    when(request.getInputStream()).thenReturn(new DelegatingServletInputStream(json.getBytes()));

    // Mock the deserialization process
    Usuario usuario = new Usuario();
    usuario.setUsername("user@utfpr.edu.br");
    usuario.setPassword("password");
    usuario.setEmailVerificado(true); // Set email as verified for test
    usuario.setPermissoes(new java.util.HashSet<>()); // Initialize permissions

    UsuarioResponseDto usuarioDto = new UsuarioResponseDto();
    usuarioDto.setUsername(usuario.getUsername());
    usuarioDto.setPermissoes(new java.util.HashSet<>());

    when(usuarioRepository.findWithPermissoesByUsernameOrEmail(anyString(), anyString()))
        .thenReturn(usuario);
    when(usuarioRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(usuario);
    when(usuarioService.hasSolicitacaoNadaConstaPendingOrCompleted(anyString())).thenReturn(false);
    when(authenticationManager.authenticate(any(Authentication.class)))
        .thenReturn(
            new UsernamePasswordAuthenticationToken(
                "user@utfpr.edu.br", "password", Collections.emptyList()));
    Authentication auth = filter.attemptAuthentication(request, response);
    assertNotNull(auth);
    assertEquals("user@utfpr.edu.br", auth.getPrincipal());
  }

  @Test
  void testAttemptAuthentication_PreconditionRequired() throws Exception {
    // Criar JSON manualmente para evitar problemas de desserialização
    String json = "{\"username\":\"user@utfpr.edu.br\",\"password\":\"password\"}";

    Usuario usuario = new Usuario();
    usuario.setUsername("user@utfpr.edu.br");
    usuario.setPassword("password");
    usuario.setPermissoes(new java.util.HashSet<>());

    when(request.getInputStream()).thenReturn(new DelegatingServletInputStream(json.getBytes()));
    // Mock ambos os métodos do repository usados no fluxo
    when(usuarioRepository.findWithPermissoesByUsernameOrEmail(anyString(), anyString()))
        .thenReturn(usuario);
    when(usuarioRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(usuario);
    when(usuarioService.hasSolicitacaoNadaConstaPendingOrCompleted(anyString())).thenReturn(true);
    Exception exception =
        assertThrows(
            PreconditionRequiredAuthenticationException.class,
            () -> filter.attemptAuthentication(request, response));
    assertTrue(exception.getMessage().contains("solicitação de nada consta"));
  }

  @Test
  void testAttemptAuthentication_InvalidCredentials() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setUsername("user@utfpr.edu.br");
    usuario.setPassword("wrong");
    usuario.setPermissoes(new java.util.HashSet<>());

    String json = "{\"username\":\"user@utfpr.edu.br\",\"password\":\"wrong\"}";
    when(request.getInputStream()).thenReturn(new DelegatingServletInputStream(json.getBytes()));
    when(usuarioRepository.findWithPermissoesByUsernameOrEmail(anyString(), anyString()))
        .thenReturn(usuario);
    when(usuarioRepository.findByUsernameOrEmail(anyString(), anyString())).thenReturn(usuario);
    when(usuarioService.hasSolicitacaoNadaConstaPendingOrCompleted(anyString())).thenReturn(false);
    when(authenticationManager.authenticate(any(Authentication.class)))
        .thenThrow(
            new org.springframework.security.core.AuthenticationException("Bad credentials") {});
    assertThrows(
        org.springframework.security.core.AuthenticationException.class,
        () -> filter.attemptAuthentication(request, response));
  }

  @Test
  void testSuccessfulAuthentication_GeneratesToken() throws Exception {
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    Authentication auth = mock(Authentication.class);
    when(auth.getName()).thenReturn("user@utfpr.edu.br");

    filter.successfulAuthentication(request, response, chain, auth);
    writer.flush();
    String token = stringWriter.toString();
    assertNotNull(token);
    assertFalse(token.isEmpty());
    verify(response).setContentType("application/json");
    verify(response)
        .addHeader(
            br.com.utfpr.gerenciamento.server.security.SecurityConstants.HEADER_STRING,
            br.com.utfpr.gerenciamento.server.security.SecurityConstants.TOKEN_PREFIX
                + token.trim());
  }

  @Test
  void testUnsuccessfulAuthentication_PreconditionRequired() throws Exception {
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    PreconditionRequiredAuthenticationException ex =
        new PreconditionRequiredAuthenticationException("Nada consta");
    filter.unsuccessfulAuthentication(request, response, ex);
    writer.flush();
    verify(response).setStatus(428);
    verify(response).setContentType("application/problem+json");
    String json = stringWriter.toString();
    assertTrue(json.contains("Nada consta"));
  }

  @Test
  void testUnsuccessfulAuthentication_Unauthorized() throws Exception {
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    AuthenticationException ex =
        new org.springframework.security.core.AuthenticationException("Unauthorized") {};
    filter.unsuccessfulAuthentication(request, response, ex);
    writer.flush();
    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(response).setContentType("application/problem+json");
    String json = stringWriter.toString();
    // RFC 9457 ProblemDetail usa formato padronizado com "detail"
    assertTrue(json.contains("Usuario ou senha incorretos") || json.contains("detail"));
  }

  @Test
  void testAttemptAuthentication_UsernameProfessores_NoParsing() throws Exception {
    // TESTE DE SEGURANÇA: Valida que email completo é usado sem parsing
    // Antes: user@professores.utfpr.edu.br → user@utfpr.edu.br (VULNERABILIDADE)
    // Agora: user@professores.utfpr.edu.br → user@professores.utfpr.edu.br (SEGURO)

    // Criar JSON manualmente para evitar problemas de desserialização
    String json =
        String.format(
            "{\"username\":\"%s\",\"password\":\"%s\"}",
            "user@professores.utfpr.edu.br", "password");

    Usuario usuario = new Usuario();
    usuario.setUsername("user@professores.utfpr.edu.br");
    usuario.setPassword("password");
    usuario.setEmailVerificado(true); // Importante: usuário deve ter email verificado
    usuario.setPermissoes(new java.util.HashSet<>());

    when(request.getInputStream()).thenReturn(new DelegatingServletInputStream(json.getBytes()));
    when(usuarioRepository.findWithPermissoesByUsernameOrEmail(
            "user@professores.utfpr.edu.br", "user@professores.utfpr.edu.br"))
        .thenReturn(usuario);
    when(usuarioRepository.findByUsernameOrEmail(
            "user@professores.utfpr.edu.br", "user@professores.utfpr.edu.br"))
        .thenReturn(usuario);
    when(usuarioService.hasSolicitacaoNadaConstaPendingOrCompleted("user@professores.utfpr.edu.br"))
        .thenReturn(false);
    when(authenticationManager.authenticate(any(Authentication.class)))
        .thenReturn(
            new UsernamePasswordAuthenticationToken(
                "user@professores.utfpr.edu.br", "password", Collections.emptyList()));
    Authentication auth = filter.attemptAuthentication(request, response);
    assertNotNull(auth);
    // SEGURANÇA: Confirma que email completo é mantido (sem parsing de domínio)
    assertEquals("user@professores.utfpr.edu.br", auth.getPrincipal());
  }

  @Test
  void testAttemptAuthentication_UsernameAdministrativo_NoParsing() throws Exception {
    // TESTE DE SEGURANÇA: Valida que email completo é usado sem parsing
    // Antes: user@administrativo.utfpr.edu.br → user@utfpr.edu.br (VULNERABILIDADE)
    // Agora: user@administrativo.utfpr.edu.br → user@administrativo.utfpr.edu.br (SEGURO)

    // Criar JSON manualmente para evitar problemas de desserialização
    String json =
        String.format(
            "{\"username\":\"%s\",\"password\":\"%s\"}",
            "user@administrativo.utfpr.edu.br", "password");

    Usuario usuario = new Usuario();
    usuario.setUsername("user@administrativo.utfpr.edu.br");
    usuario.setPassword("password");
    usuario.setEmailVerificado(true); // Importante: usuário deve ter email verificado
    usuario.setPermissoes(new java.util.HashSet<>());

    when(request.getInputStream()).thenReturn(new DelegatingServletInputStream(json.getBytes()));
    when(usuarioRepository.findWithPermissoesByUsernameOrEmail(
            "user@administrativo.utfpr.edu.br", "user@administrativo.utfpr.edu.br"))
        .thenReturn(usuario);
    when(usuarioRepository.findByUsernameOrEmail(
            "user@administrativo.utfpr.edu.br", "user@administrativo.utfpr.edu.br"))
        .thenReturn(usuario);
    when(usuarioService.hasSolicitacaoNadaConstaPendingOrCompleted(
            "user@administrativo.utfpr.edu.br"))
        .thenReturn(false);
    when(authenticationManager.authenticate(any(Authentication.class)))
        .thenReturn(
            new UsernamePasswordAuthenticationToken(
                "user@administrativo.utfpr.edu.br", "password", Collections.emptyList()));
    Authentication auth = filter.attemptAuthentication(request, response);
    assertNotNull(auth);
    // SEGURANÇA: Confirma que email completo é mantido (sem parsing de domínio)
    assertEquals("user@administrativo.utfpr.edu.br", auth.getPrincipal());
  }

  @Test
  void testAttemptAuthentication_UserEnumerationSecurity_ValidPasswordSpecificFeedback()
      throws Exception {
    // TESTE DE SEGURANÇA CRÍTICO: Valida que senhas corretas recebem feedback específico,
    // mas inválidas recebem mensagem genérica para prevenir user enumeration

    // Cenário 1: Usuário existe com senha correta mas tem nada consta pendente
    String jsonValidPassword =
        "{\"username\":\"user@utfpr.edu.br\",\"password\":\"correctPassword\"}";
    Usuario usuarioExistente = new Usuario();
    usuarioExistente.setUsername("user@utfpr.edu.br");
    usuarioExistente.setPassword("correctPassword");
    usuarioExistente.setEmailVerificado(true);
    usuarioExistente.setPermissoes(new java.util.HashSet<>());

    when(request.getInputStream())
        .thenReturn(new DelegatingServletInputStream(jsonValidPassword.getBytes()));
    when(usuarioRepository.findWithPermissoesByUsernameOrEmail(
            "user@utfpr.edu.br", "user@utfpr.edu.br"))
        .thenReturn(usuarioExistente);
    when(usuarioRepository.findByUsernameOrEmail("user@utfpr.edu.br", "user@utfpr.edu.br"))
        .thenReturn(usuarioExistente);
    when(usuarioService.hasSolicitacaoNadaConstaPendingOrCompleted("user@utfpr.edu.br"))
        .thenReturn(true);

    // Mock autenticação bem-sucedida (senha correta)
    when(authenticationManager.authenticate(any(Authentication.class)))
        .thenReturn(
            new UsernamePasswordAuthenticationToken(
                "user@utfpr.edu.br", "correctPassword", Collections.emptyList()));

    // Usuário com senha correta deve receber 428 (nada consta)
    Exception exceptionNadaConsta =
        assertThrows(
            PreconditionRequiredAuthenticationException.class,
            () -> filter.attemptAuthentication(request, response));
    assertTrue(exceptionNadaConsta.getMessage().contains("nada consta"));

    // Cenário 2: Usuário existe com senha errada recebe mensagem genérica
    String jsonWrongPassword =
        "{\"username\":\"user@utfpr.edu.br\",\"password\":\"wrongPassword\"}";
    when(request.getInputStream())
        .thenReturn(new DelegatingServletInputStream(jsonWrongPassword.getBytes()));

    // Mock autenticação falha (senha errada)
    when(authenticationManager.authenticate(any(Authentication.class)))
        .thenThrow(new BadCredentialsException("Credenciais inválidas"));

    // Usuário com senha errada deve receber BadCredentialsException genérica
    Exception exceptionWrongPassword =
        assertThrows(
            BadCredentialsException.class, () -> filter.attemptAuthentication(request, response));
    assertEquals("Credenciais inválidas", exceptionWrongPassword.getMessage());

    // Cenário 3: Usuário não existe recebe mensagem genérica (para comparação)
    String jsonNonExistentUser =
        "{\"username\":\"nonexistent@utfpr.edu.br\",\"password\":\"anyPassword\"}";
    when(request.getInputStream())
        .thenReturn(new DelegatingServletInputStream(jsonNonExistentUser.getBytes()));
    when(usuarioRepository.findWithPermissoesByUsernameOrEmail(
            "nonexistent@utfpr.edu.br", "nonexistent@utfpr.edu.br"))
        .thenReturn(null);

    // Usuário não existente deve receber BadCredentialsException genérica
    Exception exceptionNonExistent =
        assertThrows(
            BadCredentialsException.class, () -> filter.attemptAuthentication(request, response));
    assertEquals("Credenciais inválidas", exceptionNonExistent.getMessage());

    // SEGURANÇA: Ambos os cenários (senha errada e usuário não existe) retornam a mesma mensagem
    // genérica
    assertEquals(exceptionWrongPassword.getMessage(), exceptionNonExistent.getMessage());
  }

  // Helper for simulating ServletInputStream from byte[]
  static class DelegatingServletInputStream extends jakarta.servlet.ServletInputStream {
    private final byte[] data;
    private int idx = 0;

    DelegatingServletInputStream(byte[] data) {
      this.data = data;
    }

    @Override
    public int read() {
      return idx < data.length ? data[idx++] & 0xFF : -1;
    }

    @Override
    public boolean isFinished() {
      return idx >= data.length;
    }

    @Override
    public boolean isReady() {
      return true;
    }

    @Override
    public void setReadListener(jakarta.servlet.ReadListener readListener) {
      throw new UnsupportedOperationException("Implementar");
    }
  }
}
