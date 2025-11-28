package br.com.utfpr.gerenciamento.server.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Testes unitários adicionais para SecurityUtils focados nos métodos utilizados pelos controllers
 * para filtros por role.
 */
@ExtendWith(MockitoExtension.class)
class SecurityUtilsRoleFilterTest {

  @Test
  void getAuthenticatedUsername_comContextPopulado_deveRetornarUsername() {
    try (MockedStatic<SecurityContextHolder> contextHolder =
        mockStatic(SecurityContextHolder.class)) {
      // Arrange
      Authentication auth = mock(Authentication.class);
      SecurityContext context = mock(SecurityContext.class);

      contextHolder.when(SecurityContextHolder::getContext).thenReturn(context);
      when(context.getAuthentication()).thenReturn(auth);
      when(auth.getName()).thenReturn("usuario@test.com");

      // Act
      String username = SecurityUtils.getAuthenticatedUsername();

      // Assert
      assertEquals("usuario@test.com", username);
      verify(context).getAuthentication();
      verify(auth).getName();
    }
  }

  @Test
  void getAuthenticatedUserRoles_comContextNulo_deveLancarExcecao() {
    try (MockedStatic<SecurityContextHolder> contextHolder =
        mockStatic(SecurityContextHolder.class)) {
      // Arrange
      mock(SecurityContext.class);
      contextHolder.when(SecurityContextHolder::getContext).thenReturn(null);

      // Act & Assert
      IllegalStateException ex =
          assertThrows(IllegalStateException.class, SecurityUtils::getAuthenticatedUserRoles);

      assertTrue(ex.getMessage().contains("Authentication não pode ser null"));
    }
  }

  @Test
  void getAuthenticatedUserRoles_comListaVazia_deveRetornarListaVazia() {
    try (MockedStatic<SecurityContextHolder> contextHolder =
        mockStatic(SecurityContextHolder.class)) {
      // Arrange
      Collection<GrantedAuthority> authorities = List.of();

      Authentication auth = mock(Authentication.class);
      SecurityContext context = mock(SecurityContext.class);

      contextHolder.when(SecurityContextHolder::getContext).thenReturn(context);
      when(context.getAuthentication()).thenReturn(auth);
      when(auth.getAuthorities()).thenReturn((Collection) authorities);

      // Act
      List<String> roles = SecurityUtils.getAuthenticatedUserRoles();

      // Assert
      assertTrue(roles.isEmpty());
      verify(auth).getAuthorities();
    }
  }

  @Test
  void getAuthenticatedUserRoles_comMultiplasRolesIncluindoProfessor_deveRetornarTodasRoles() {
    try (MockedStatic<SecurityContextHolder> contextHolder =
        mockStatic(SecurityContextHolder.class)) {
      // Arrange
      Collection<GrantedAuthority> authorities =
          List.of(
              new SimpleGrantedAuthority("ROLE_PROFESSOR"),
              new SimpleGrantedAuthority("ROLE_ALUNO"),
              new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"));

      Authentication auth = mock(Authentication.class);
      SecurityContext context = mock(SecurityContext.class);

      contextHolder.when(SecurityContextHolder::getContext).thenReturn(context);
      when(context.getAuthentication()).thenReturn(auth);
      when(auth.getAuthorities()).thenReturn((Collection) authorities);

      // Act
      List<String> roles = SecurityUtils.getAuthenticatedUserRoles();

      // Assert
      assertEquals(3, roles.size());
      assertTrue(roles.contains("ROLE_PROFESSOR"));
      assertTrue(roles.contains("ROLE_ALUNO"));
      assertTrue(roles.contains("ROLE_ADMINISTRADOR"));
    }
  }

  @Test
  void getAuthenticatedUserRoles_comRoleLaboratorista_deveRetornarApenasRoleLaboratorista() {
    try (MockedStatic<SecurityContextHolder> contextHolder =
        mockStatic(SecurityContextHolder.class)) {
      // Arrange
      Collection<GrantedAuthority> authorities =
          List.of(new SimpleGrantedAuthority("ROLE_LABORATORISTA"));

      Authentication auth = mock(Authentication.class);
      SecurityContext context = mock(SecurityContext.class);

      contextHolder.when(SecurityContextHolder::getContext).thenReturn(context);
      when(context.getAuthentication()).thenReturn(auth);
      when(auth.getAuthorities()).thenReturn((Collection) authorities);

      // Act
      List<String> roles = SecurityUtils.getAuthenticatedUserRoles();

      // Assert
      assertEquals(1, roles.size());
      assertTrue(roles.contains("ROLE_LABORATORISTA"));
    }
  }

  @Test
  void getAuthenticatedUsername_comContextNulo_deveLancarExcecaoContextNulo() {
    try (MockedStatic<SecurityContextHolder> contextHolder =
        mockStatic(SecurityContextHolder.class)) {
      // Arrange
      contextHolder.when(SecurityContextHolder::getContext).thenReturn(null);

      // Act & Assert
      IllegalStateException ex =
          assertThrows(IllegalStateException.class, SecurityUtils::getAuthenticatedUsername);

      assertTrue(ex.getMessage().contains("Authentication não pode ser null"));
    }
  }
}
