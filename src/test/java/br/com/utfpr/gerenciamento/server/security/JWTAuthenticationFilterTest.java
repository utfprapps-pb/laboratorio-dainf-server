package br.com.utfpr.gerenciamento.server.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.utfpr.gerenciamento.server.dto.UsuarioResponseDto;
import br.com.utfpr.gerenciamento.server.model.Usuario;
import br.com.utfpr.gerenciamento.server.service.impl.UsuarioServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

class JWTAuthenticationFilterTest {
  private AuthenticationManager authenticationManager;
  private UsuarioServiceImpl usuarioService;
  private JWTAuthenticationFilter filter;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private FilterChain chain;

  @BeforeEach
  void setUp() {
    authenticationManager = mock(AuthenticationManager.class);
    usuarioService = mock(UsuarioServiceImpl.class);
    var env = mock(org.springframework.core.env.Environment.class);
    when(env.getProperty(anyString())).thenReturn("test-secret");
    filter = new JWTAuthenticationFilter(authenticationManager, usuarioService, env);
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    chain = mock(FilterChain.class);
  }

  @Test
  void shouldReturn428IfNadaConstaSolicitacaoEmAberto() throws IOException {
    Usuario usuario = new Usuario();
    usuario.setUsername("user@utfpr.edu.br");
    usuario.setPassword("pass");
    usuario.setPermissoes(new java.util.HashSet<>());
    UsuarioResponseDto usuarioDto = new UsuarioResponseDto();
    usuarioDto.setUsername(usuario.getUsername());
    usuarioDto.setPermissoes(new java.util.HashSet<>());
    when(request.getInputStream())
        .thenReturn(
            new DelegatingServletInputStream(new ObjectMapper().writeValueAsBytes(usuario)));
    when(usuarioService.findByUsernameForAuthentication(anyString())).thenReturn(usuarioDto);
    doAnswer(invocation -> usuario).when(usuarioService).toEntity(any());
    when(usuarioService.hasSolicitacaoNadaConstaPendingOrCompleted(anyString())).thenReturn(true);
    assertThrows(
        PreconditionRequiredAuthenticationException.class,
        () -> filter.attemptAuthentication(request, response));
  }

  @Test
  void unsuccessfulAuthenticationShouldReturnJsonAnd428ForNadaConsta()
      throws IOException, ServletException {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    PreconditionRequiredAuthenticationException ex =
        new PreconditionRequiredAuthenticationException(
            "Foi realizado uma solicitação de nada consta para o usuário. Contate a administração.");
    filter.unsuccessfulAuthentication(request, response, ex);
    verify(response).setStatus(428);
    verify(response).setContentType("application/json");
    pw.flush();
    String json = sw.toString();
    assertTrue(json.contains("nada consta"));
    assertTrue(json.contains("error"));
  }

  @Test
  void testAttemptAuthentication_Success() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setUsername("user@utfpr.edu.br");
    usuario.setPassword("password");
    usuario.setPermissoes(new java.util.HashSet<>());
    UsuarioResponseDto usuarioDto = new UsuarioResponseDto();
    usuarioDto.setUsername(usuario.getUsername());
    usuarioDto.setPermissoes(new java.util.HashSet<>());
    when(request.getInputStream())
        .thenReturn(
            new DelegatingServletInputStream(new ObjectMapper().writeValueAsBytes(usuario)));
    when(usuarioService.findByUsernameForAuthentication(anyString())).thenReturn(usuarioDto);
    doAnswer(invocation -> usuario).when(usuarioService).toEntity(any());
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
    Usuario usuario = new Usuario();
    usuario.setUsername("user@utfpr.edu.br");
    usuario.setPassword("password");
    usuario.setPermissoes(new java.util.HashSet<>());
    UsuarioResponseDto usuarioDto = new UsuarioResponseDto();
    usuarioDto.setUsername(usuario.getUsername());
    usuarioDto.setPermissoes(new java.util.HashSet<>());
    when(request.getInputStream())
        .thenReturn(
            new DelegatingServletInputStream(new ObjectMapper().writeValueAsBytes(usuario)));
    when(usuarioService.findByUsernameForAuthentication(anyString())).thenReturn(usuarioDto);
    doAnswer(invocation -> usuario).when(usuarioService).toEntity(any());
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
    UsuarioResponseDto usuarioDto = new UsuarioResponseDto();
    usuarioDto.setUsername(usuario.getUsername());
    usuarioDto.setPermissoes(new java.util.HashSet<>());
    when(request.getInputStream())
        .thenReturn(
            new DelegatingServletInputStream(new ObjectMapper().writeValueAsBytes(usuario)));
    when(usuarioService.findByUsernameForAuthentication(anyString())).thenReturn(usuarioDto);
    doAnswer(invocation -> usuario).when(usuarioService).toEntity(any());
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
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);
    Authentication auth = mock(Authentication.class);
    when(auth.getName()).thenReturn("user@utfpr.edu.br");
    filter.successfulAuthentication(request, mockResponse, chain, auth);
    writer.flush();
    String token = stringWriter.toString();
    assertNotNull(token);
    assertFalse(token.isEmpty());
    verify(mockResponse).setContentType("application/json");
    verify(mockResponse)
        .addHeader(
            br.com.utfpr.gerenciamento.server.security.SecurityConstants.HEADER_STRING,
            br.com.utfpr.gerenciamento.server.security.SecurityConstants.TOKEN_PREFIX
                + token.trim());
  }

  @Test
  void testUnsuccessfulAuthentication_PreconditionRequired() throws Exception {
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);
    PreconditionRequiredAuthenticationException ex =
        new PreconditionRequiredAuthenticationException("Nada consta");
    filter.unsuccessfulAuthentication(request, mockResponse, ex);
    writer.flush();
    verify(mockResponse).setStatus(428);
    String json = stringWriter.toString();
    assertTrue(json.contains("Nada consta"));
  }

  @Test
  void testUnsuccessfulAuthentication_Unauthorized() throws Exception {
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);
    AuthenticationException ex =
        new org.springframework.security.core.AuthenticationException("Unauthorized") {};
    filter.unsuccessfulAuthentication(request, mockResponse, ex);
    writer.flush();
    verify(mockResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    String json = stringWriter.toString();
    assertTrue(json.contains("Unauthorized"));
  }

  @Test
  void testAttemptAuthentication_UsernameProfessores() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setUsername("user@professores.utfpr.edu.br");
    usuario.setPassword("password");
    usuario.setPermissoes(new java.util.HashSet<>()); // Correção definitiva
    UsuarioResponseDto usuarioDto = new UsuarioResponseDto();
    usuarioDto.setUsername("user@utfpr.edu.br");
    usuarioDto.setPermissoes(new java.util.HashSet<>());
    when(request.getInputStream())
        .thenReturn(
            new DelegatingServletInputStream(new ObjectMapper().writeValueAsBytes(usuario)));
    when(usuarioService.findByUsernameForAuthentication("user@utfpr.edu.br"))
        .thenReturn(usuarioDto);
    doAnswer(invocation -> usuario).when(usuarioService).toEntity(any());
    when(usuarioService.hasSolicitacaoNadaConstaPendingOrCompleted("user@utfpr.edu.br"))
        .thenReturn(false);
    when(authenticationManager.authenticate(any(Authentication.class)))
        .thenReturn(
            new UsernamePasswordAuthenticationToken(
                "user@utfpr.edu.br", "password", Collections.emptyList()));
    Authentication auth = filter.attemptAuthentication(request, response);
    assertNotNull(auth);
    assertEquals("user@utfpr.edu.br", auth.getPrincipal());
  }

  @Test
  void testAttemptAuthentication_UsernameAdministrativo() throws Exception {
    Usuario usuario = new Usuario();
    usuario.setUsername("user@administrativo.utfpr.edu.br");
    usuario.setPassword("password");
    usuario.setPermissoes(new java.util.HashSet<>()); // Correção definitiva
    UsuarioResponseDto usuarioDto = new UsuarioResponseDto();
    usuarioDto.setUsername("user@utfpr.edu.br");
    usuarioDto.setPermissoes(new java.util.HashSet<>());
    when(request.getInputStream())
        .thenReturn(
            new DelegatingServletInputStream(new ObjectMapper().writeValueAsBytes(usuario)));
    when(usuarioService.findByUsernameForAuthentication("user@utfpr.edu.br"))
        .thenReturn(usuarioDto);
    doAnswer(invocation -> usuario).when(usuarioService).toEntity(any());
    when(usuarioService.hasSolicitacaoNadaConstaPendingOrCompleted("user@utfpr.edu.br"))
        .thenReturn(false);
    when(authenticationManager.authenticate(any(Authentication.class)))
        .thenReturn(
            new UsernamePasswordAuthenticationToken(
                "user@utfpr.edu.br", "password", Collections.emptyList()));
    Authentication auth = filter.attemptAuthentication(request, response);
    assertNotNull(auth);
    assertEquals("user@utfpr.edu.br", auth.getPrincipal());
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
    public void setReadListener(jakarta.servlet.ReadListener readListener) {}
  }
}
