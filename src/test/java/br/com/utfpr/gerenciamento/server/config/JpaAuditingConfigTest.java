package br.com.utfpr.gerenciamento.server.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

class JpaAuditingConfigTest {
  private final JpaAuditingConfig config = new JpaAuditingConfig();

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void auditorProviderReturnsSystemWhenNoAuthentication() {
    SecurityContextHolder.clearContext();
    Optional<String> auditor = config.auditorProvider().getCurrentAuditor();
    assertEquals("system", auditor.orElse(null));
  }

  @Test
  void auditorProviderReturnsSystemWhenNotAuthenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(null, null, null));
    Optional<String> auditor = config.auditorProvider().getCurrentAuditor();
    assertEquals("system", auditor.orElse(null));
  }

  @Test
  void auditorProviderReturnsUsernameForUserDetails() {
    UserDetails userDetails =
        new UserDetails() {
          @Override
          public String getUsername() {
            return "userDetailsName";
          }

          @Override
          public String getPassword() {
            return "pass";
          }

          @Override
          public boolean isAccountNonExpired() {
            return true;
          }

          @Override
          public boolean isAccountNonLocked() {
            return true;
          }

          @Override
          public boolean isCredentialsNonExpired() {
            return true;
          }

          @Override
          public boolean isEnabled() {
            return true;
          }

          @Override
          public java.util.Collection getAuthorities() {
            return null;
          }
        };
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, null));
    Optional<String> auditor = config.auditorProvider().getCurrentAuditor();
    assertEquals("userDetailsName", auditor.orElse(null));
  }

  @Test
  void auditorProviderReturnsPrincipalString() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("principalString", null, null));
    Optional<String> auditor = config.auditorProvider().getCurrentAuditor();
    assertEquals("principalString", auditor.orElse(null));
  }

  @Test
  void auditorProviderReturnsSystemForOtherPrincipalTypes() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(12345, null, null));
    Optional<String> auditor = config.auditorProvider().getCurrentAuditor();
    assertEquals("system", auditor.orElse(null));
  }
}
