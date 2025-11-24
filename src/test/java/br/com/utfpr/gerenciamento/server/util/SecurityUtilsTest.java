package br.com.utfpr.gerenciamento.server.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class SecurityUtilsTest {

  @Test
  void constructorPrivado_deveLancarExcecao() throws Exception {
    var constructor = SecurityUtils.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    var exception =
        assertThrows(java.lang.reflect.InvocationTargetException.class, constructor::newInstance);

    // Verifica que a causa raiz é UnsupportedOperationException
    assertEquals(UnsupportedOperationException.class, exception.getCause().getClass());
    assertEquals("Utility class cannot be instantiated", exception.getCause().getMessage());
  }

  @Test
  void extractUsername_comAuthenticationNull_deveLancarExcecao() {
    IllegalStateException ex =
        assertThrows(IllegalStateException.class, () -> SecurityUtils.extractUsername(null));
    assertEquals("Authentication não pode ser null", ex.getMessage());
  }

  @Test
  void extractUsername_comPrincipalString_deveExtrairUsername() {
    Authentication auth = new UsernamePasswordAuthenticationToken("usuario@test.com", "senha");
    String username = SecurityUtils.extractUsername(auth);
    assertEquals("usuario@test.com", username);
  }

  @Test
  void extractUsername_comPrincipalUserDetails_deveExtrairUsername() {
    UserDetails userDetails =
        User.builder()
            .username("user@utfpr.edu.br")
            .password("senha")
            .authorities("ROLE_USER")
            .build();
    Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, "senha");

    String username = SecurityUtils.extractUsername(auth);
    assertEquals("user@utfpr.edu.br", username);
  }

  @Test
  void extractUsername_comGetNameValido_devePriorizarGetName() {
    Authentication auth = mock(Authentication.class);
    when(auth.getName()).thenReturn("username@test.com");

    String username = SecurityUtils.extractUsername(auth);
    assertEquals("username@test.com", username);

    // Verifica que não tentou acessar principal (getName() foi suficiente)
    verify(auth, never()).getPrincipal();
  }

  @Test
  void extractUsername_comGetNameVazio_deveTentarUserDetails() {
    UserDetails userDetails =
        User.builder()
            .username("fallback@test.com")
            .password("senha")
            .authorities("ROLE_USER")
            .build();

    Authentication auth = mock(Authentication.class);
    when(auth.getName()).thenReturn(""); // getName() retorna vazio
    when(auth.getPrincipal()).thenReturn(userDetails);

    String username = SecurityUtils.extractUsername(auth);
    assertEquals("fallback@test.com", username);
  }

  @Test
  void extractUsername_comGetNameNull_deveTentarUserDetails() {
    UserDetails userDetails =
        User.builder()
            .username("fallback@test.com")
            .password("senha")
            .authorities("ROLE_USER")
            .build();

    Authentication auth = mock(Authentication.class);
    when(auth.getName()).thenReturn(null); // getName() retorna null
    when(auth.getPrincipal()).thenReturn(userDetails);

    String username = SecurityUtils.extractUsername(auth);
    assertEquals("fallback@test.com", username);
  }

  @Test
  void extractUsername_comGetNameEUserDetailsVazios_deveTentarStringPrincipal() {
    Authentication auth = mock(Authentication.class);
    when(auth.getName()).thenReturn(null);

    // UserDetails com username vazio
    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("");
    when(auth.getPrincipal()).thenReturn(userDetails);

    // Este teste deve lançar exceção pois UserDetails tem username vazio
    assertThrows(IllegalStateException.class, () -> SecurityUtils.extractUsername(auth));
  }

  @Test
  void extractUsername_comPrincipalStringDirecto_deveExtrairUsername() {
    Authentication auth = mock(Authentication.class);
    when(auth.getName()).thenReturn(null);
    when(auth.getPrincipal()).thenReturn("direct@test.com");

    String username = SecurityUtils.extractUsername(auth);
    assertEquals("direct@test.com", username);
  }

  @Test
  void extractUsername_comPrincipalInvalido_deveLancarExcecao() {
    Authentication auth = mock(Authentication.class);
    when(auth.getName()).thenReturn(null);
    when(auth.getPrincipal()).thenReturn(new Object()); // Tipo inválido

    IllegalStateException ex =
        assertThrows(IllegalStateException.class, () -> SecurityUtils.extractUsername(auth));

    assertTrue(ex.getMessage().contains("Principal type: java.lang.Object"));
  }

  @Test
  void extractUsername_comUsernameVazio_deveLancarExcecao() {
    Authentication auth = mock(Authentication.class);
    when(auth.getName()).thenReturn("   "); // Apenas espaços
    when(auth.getPrincipal()).thenReturn("   ");

    assertThrows(IllegalStateException.class, () -> SecurityUtils.extractUsername(auth));
  }

  @Test
  void extractUsername_comPrincipalNull_deveLancarExcecao() {
    Authentication auth = mock(Authentication.class);
    when(auth.getName()).thenReturn(null);
    when(auth.getPrincipal()).thenReturn(null);

    IllegalStateException ex =
        assertThrows(IllegalStateException.class, () -> SecurityUtils.extractUsername(auth));
    assertTrue(ex.getMessage().contains("Principal type: null"));
  }

  @Test
  void extractUsername_comUserDetailsUsernameEspacos_deveLancarExcecao() {
    UserDetails userDetails =
        User.builder().username("   ").password("senha").authorities("ROLE_USER").build();

    Authentication auth = mock(Authentication.class);
    when(auth.getName()).thenReturn(null);
    when(auth.getPrincipal()).thenReturn(userDetails);

    assertThrows(IllegalStateException.class, () -> SecurityUtils.extractUsername(auth));
  }
}
