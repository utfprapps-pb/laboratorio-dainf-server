package br.com.utfpr.gerenciamento.server.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Testes unitários para AuditRevisionListener.
 *
 * <p>Testa a captura de usuário autenticado e IP da requisição durante operações auditadas.
 */
@DisplayName("AuditRevisionListener")
class AuditRevisionListenerTest {

  private AuditRevisionListener listener;
  private AuditRevision revision;
  private MockedStatic<SecurityContextHolder> securityContextHolderMock;
  private MockedStatic<RequestContextHolder> requestContextHolderMock;

  @BeforeEach
  void setUp() {
    listener = new AuditRevisionListener();
    revision = new AuditRevision();
    securityContextHolderMock = mockStatic(SecurityContextHolder.class);
    requestContextHolderMock = mockStatic(RequestContextHolder.class);
  }

  @AfterEach
  void tearDown() {
    securityContextHolderMock.close();
    requestContextHolderMock.close();
  }

  @Nested
  @DisplayName("Captura de Usuário")
  class CapturaUsuario {

    @Test
    @DisplayName("Deve capturar username do usuário autenticado")
    void deveCapturarUsernameDoUsuarioAutenticado() {
      // Arrange
      SecurityContext securityContext = mock(SecurityContext.class);
      Authentication authentication = mock(Authentication.class);

      when(authentication.isAuthenticated()).thenReturn(true);
      when(authentication.getName()).thenReturn("joao.silva");
      when(authentication.getPrincipal()).thenReturn("joao.silva");
      when(securityContext.getAuthentication()).thenReturn(authentication);
      securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
      requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

      // Act
      listener.newRevision(revision);

      // Assert
      assertThat(revision.getUsuario()).isEqualTo("joao.silva");
    }

    @Test
    @DisplayName("Deve retornar 'system' quando não há autenticação")
    void deveRetornarSystemQuandoNaoHaAutenticacao() {
      // Arrange
      SecurityContext securityContext = mock(SecurityContext.class);
      when(securityContext.getAuthentication()).thenReturn(null);
      securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
      requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

      // Act
      listener.newRevision(revision);

      // Assert
      assertThat(revision.getUsuario()).isEqualTo("system");
    }

    @Test
    @DisplayName("Deve retornar 'system' quando usuário não está autenticado")
    void deveRetornarSystemQuandoUsuarioNaoEstaAutenticado() {
      // Arrange
      SecurityContext securityContext = mock(SecurityContext.class);
      Authentication authentication = mock(Authentication.class);

      when(authentication.isAuthenticated()).thenReturn(false);
      when(securityContext.getAuthentication()).thenReturn(authentication);
      securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
      requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

      // Act
      listener.newRevision(revision);

      // Assert
      assertThat(revision.getUsuario()).isEqualTo("system");
    }

    @Test
    @DisplayName("Deve retornar 'system' para usuário anônimo")
    void deveRetornarSystemParaUsuarioAnonimo() {
      // Arrange
      SecurityContext securityContext = mock(SecurityContext.class);
      Authentication authentication = mock(Authentication.class);

      when(authentication.isAuthenticated()).thenReturn(true);
      when(authentication.getPrincipal()).thenReturn("anonymousUser");
      when(securityContext.getAuthentication()).thenReturn(authentication);
      securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
      requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

      // Act
      listener.newRevision(revision);

      // Assert
      assertThat(revision.getUsuario()).isEqualTo("system");
    }
  }

  @Nested
  @DisplayName("Captura de IP")
  class CapturaIp {

    @Test
    @DisplayName("Deve capturar IP direto do request")
    void deveCapturarIpDiretoDoRequest() {
      // Arrange
      configurarUsuarioAutenticado();

      HttpServletRequest request = mock(HttpServletRequest.class);
      ServletRequestAttributes attrs = mock(ServletRequestAttributes.class);

      when(attrs.getRequest()).thenReturn(request);
      when(request.getHeader("X-Forwarded-For")).thenReturn(null);
      when(request.getRemoteAddr()).thenReturn("192.168.1.100");
      requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(attrs);

      // Act
      listener.newRevision(revision);

      // Assert
      assertThat(revision.getIp()).isEqualTo("192.168.1.100");
    }

    @Test
    @DisplayName("Deve usar X-Forwarded-For quando presente")
    void deveUsarXForwardedForQuandoPresente() {
      // Arrange
      configurarUsuarioAutenticado();

      HttpServletRequest request = mock(HttpServletRequest.class);
      ServletRequestAttributes attrs = mock(ServletRequestAttributes.class);

      when(attrs.getRequest()).thenReturn(request);
      when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.50");
      requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(attrs);

      // Act
      listener.newRevision(revision);

      // Assert
      assertThat(revision.getIp()).isEqualTo("203.0.113.50");
    }

    @Test
    @DisplayName("Deve extrair primeiro IP de X-Forwarded-For com múltiplos IPs")
    void deveExtrairPrimeiroIpDeXForwardedForComMultiplosIps() {
      // Arrange
      configurarUsuarioAutenticado();

      HttpServletRequest request = mock(HttpServletRequest.class);
      ServletRequestAttributes attrs = mock(ServletRequestAttributes.class);

      when(attrs.getRequest()).thenReturn(request);
      when(request.getHeader("X-Forwarded-For"))
          .thenReturn("203.0.113.50, 70.41.3.18, 150.172.238.178");
      requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(attrs);

      // Act
      listener.newRevision(revision);

      // Assert
      assertThat(revision.getIp()).isEqualTo("203.0.113.50");
    }

    @Test
    @DisplayName("Deve retornar null quando não há request attributes")
    void deveRetornarNullQuandoNaoHaRequestAttributes() {
      // Arrange
      configurarUsuarioAutenticado();
      requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

      // Act
      listener.newRevision(revision);

      // Assert
      assertThat(revision.getIp()).isNull();
    }

    @Test
    @DisplayName("Deve suportar endereço IPv6")
    void deveSuportarEnderecoIpv6() {
      // Arrange
      configurarUsuarioAutenticado();

      HttpServletRequest request = mock(HttpServletRequest.class);
      ServletRequestAttributes attrs = mock(ServletRequestAttributes.class);

      when(attrs.getRequest()).thenReturn(request);
      when(request.getHeader("X-Forwarded-For")).thenReturn(null);
      when(request.getRemoteAddr()).thenReturn("2001:0db8:85a3:0000:0000:8a2e:0370:7334");
      requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(attrs);

      // Act
      listener.newRevision(revision);

      // Assert
      assertThat(revision.getIp()).isEqualTo("2001:0db8:85a3:0000:0000:8a2e:0370:7334");
    }

    private void configurarUsuarioAutenticado() {
      SecurityContext securityContext = mock(SecurityContext.class);
      Authentication authentication = mock(Authentication.class);

      when(authentication.isAuthenticated()).thenReturn(true);
      when(authentication.getName()).thenReturn("usuario.teste");
      when(authentication.getPrincipal()).thenReturn("usuario.teste");
      when(securityContext.getAuthentication()).thenReturn(authentication);
      securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);
    }
  }
}
