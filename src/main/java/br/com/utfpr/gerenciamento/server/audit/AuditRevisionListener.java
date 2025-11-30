package br.com.utfpr.gerenciamento.server.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Listener para capturar informações do usuário durante operações auditadas.
 *
 * <p>Captura automaticamente o username do usuário autenticado via Spring Security e o endereço IP
 * da requisição HTTP (com suporte a X-Forwarded-For para proxies).
 *
 * @author Rodrigo Izidoro
 * @see AuditRevision
 */
public class AuditRevisionListener implements RevisionListener {

  private static final String ANONYMOUS_USER = "anonymousUser";
  private static final String SYSTEM_USER = "system";
  private static final String X_FORWARDED_FOR = "X-Forwarded-For";
  private static final int MAX_IP_LENGTH = 45;

  /**
   * Popula os metadados da revisão com informações do usuário e requisição.
   *
   * @param revisionEntity entidade de revisão a ser populada
   */
  @Override
  public void newRevision(Object revisionEntity) {
    AuditRevision revision = (AuditRevision) revisionEntity;
    revision.setUsuario(obterUsuarioLogado());
    revision.setIp(obterIpRequisicao());
  }

  private String obterUsuarioLogado() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !auth.isAuthenticated()) {
      return SYSTEM_USER;
    }

    Object principal = auth.getPrincipal();

    if (principal instanceof String && ANONYMOUS_USER.equals(principal)) {
      return SYSTEM_USER;
    }

    return auth.getName();
  }

  private String obterIpRequisicao() {
    try {
      ServletRequestAttributes attrs =
          (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

      if (attrs == null) {
        return null;
      }

      HttpServletRequest request = attrs.getRequest();
      String ip = extrairIp(request);
      return truncarIp(ip);
    } catch (Exception e) {
      return null;
    }
  }

  private String extrairIp(HttpServletRequest request) {
    String forwardedFor = request.getHeader(X_FORWARDED_FOR);

    if (forwardedFor != null && !forwardedFor.isBlank()) {
      String[] ips = forwardedFor.split(",");
      if (ips.length > 0) {
        return ips[0].trim();
      }
    }

    return request.getRemoteAddr();
  }

  private String truncarIp(String ip) {
    if (ip == null || ip.isBlank()) {
      return null;
    }

    String sanitized = ip.replaceAll("[^0-9a-fA-F:.\\[\\]]", "");

    if (sanitized.length() > MAX_IP_LENGTH) {
      return sanitized.substring(0, MAX_IP_LENGTH);
    }

    return sanitized;
  }
}
