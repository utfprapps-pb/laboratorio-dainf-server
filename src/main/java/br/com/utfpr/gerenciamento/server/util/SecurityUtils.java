package br.com.utfpr.gerenciamento.server.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

/**
 * Utilitários para operações de segurança do Spring Security.
 *
 * <p>Fornece métodos auxiliares para extração segura de informações de autenticação, com suporte a
 * diferentes configurações do Spring Security (UserDetails, string principal).
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-22
 */
public final class SecurityUtils {

  private SecurityUtils() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Extrai o username do usuário autenticado atual.
   *
   * <p>Metodo de conveniência que obtém o Authentication do SecurityContext e extrai o username de
   * forma segura.
   *
   * <p><b>Uso:</b>
   *
   * <pre>{@code
   * String username = SecurityUtils.getAuthenticatedUsername();
   * Usuario usuario = usuarioService.findByUsername(username);
   * }</pre>
   *
   * @return Username do usuário autenticado
   * @throws IllegalStateException se não houver usuário autenticado ou username não puder ser
   *     extraído
   */
  public static String getAuthenticatedUsername() {
    var context = SecurityContextHolder.getContext();
    if (context == null) {
      throw new IllegalStateException("Authentication não pode ser null");
    }
    Authentication auth = context.getAuthentication();
    return extractUsername(auth);
  }

  /**
   * Extrai o username do Authentication de forma segura.
   *
   * <p>Suporta três cenários comuns do Spring Security:
   *
   * <ol>
   *   <li>{@code auth.getName()} - funciona para ambos String e UserDetails
   *   <li>{@code Principal instanceof UserDetails} - extrai via {@code getUsername()}
   *   <li>{@code Principal instanceof String} - cast direto (compatibilidade retroativa)
   * </ol>
   *
   * <p><b>Estratégia de Extração:</b>
   *
   * <pre>
   * 1. Valida auth != null
   * 2. Tenta auth.getName() (abordagem preferencial)
   * 3. Fallback para UserDetails.getUsername()
   * 4. Fallback para String principal (configurações antigas)
   * 5. Lança exceção se nenhuma estratégia funcionar
   * </pre>
   *
   * <p><b>Exemplo de uso:</b>
   *
   * <pre>{@code
   * Authentication auth = SecurityContextHolder.getContext().getAuthentication();
   * String username = SecurityUtils.extractUsername(auth);
   * }</pre>
   *
   * @param auth Authentication do SecurityContext (não pode ser null)
   * @return Username extraído do authentication (nunca null ou vazio)
   * @throws IllegalStateException se authentication for null ou username não puder ser extraído
   */
  public static String extractUsername(Authentication auth) {
    if (auth == null) {
      throw new IllegalStateException("Authentication não pode ser null");
    }

    String username = auth.getName();
    if (username != null && !username.trim().isEmpty()) {
      return username;
    }

    Object principal = auth.getPrincipal();
    if (principal instanceof UserDetails userDetails) {
      username = userDetails.getUsername();
      if (username != null && !username.trim().isEmpty()) {
        return username;
      }
    }

    if (principal instanceof String stringPrincipal && !stringPrincipal.trim().isEmpty()) {
      return stringPrincipal;
    }

    throw new IllegalStateException(
        "Não foi possível extrair username do Authentication. Principal type: "
            + (principal != null ? principal.getClass().getName() : "null"));
  }

  /**
   * Extrai as roles/autoridades do usuário autenticado atual.
   *
   * <p>Retorna uma coleção de autoridades (roles) do usuário autenticado no contexto.
   *
   * @return Lista de strings com as autoridades do usuário (ex: ["ROLE_ADMINISTRADOR"])
   * @throws IllegalStateException se não houver usuário autenticado
   */
  public static List<String> getAuthenticatedUserRoles() {
    var context = SecurityContextHolder.getContext();
    if (context == null) {
      throw new IllegalStateException("Authentication não pode ser null");
    }
    Authentication auth = context.getAuthentication();
    if (auth == null) {
      throw new IllegalStateException("Authentication não pode ser null");
    }

    Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
    return authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .toList();
  }
}
