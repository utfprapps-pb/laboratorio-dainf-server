package br.com.utfpr.gerenciamento.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propriedades de configuracao para rate limiting.
 *
 * <p>Permite configurar limites de requisicoes por endpoint via application.properties.
 *
 * <p>Exemplo de configuracao:
 *
 * <pre>
 * rate-limit.login.requests=5
 * rate-limit.login.duration-minutes=15
 * </pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

  /** Configuracao para endpoint /login */
  private EndpointLimit login = new EndpointLimit(5, 15);

  /** Configuracao para endpoint /auth (OAuth) */
  private EndpointLimit auth = new EndpointLimit(10, 15);

  /** Configuracao para endpoint /usuario/request-code-reset-password */
  private EndpointLimit passwordResetRequest = new EndpointLimit(3, 60);

  /** Configuracao para endpoint /usuario/new-user */
  private EndpointLimit newUser = new EndpointLimit(3, 60);

  /** Configuracao para endpoint /usuario/reset-password */
  private EndpointLimit passwordReset = new EndpointLimit(5, 15);

  /** Configuracao para endpoint /usuario/resend-confirm-email */
  private EndpointLimit resendEmail = new EndpointLimit(5, 60);

  /** Configuracao para endpoint /usuario/confirm-email */
  private EndpointLimit confirmEmail = new EndpointLimit(10, 15);

  /** Habilita ou desabilita o rate limiting globalmente */
  private boolean enabled = true;

  /** Representa a configuracao de limite para um endpoint especifico. */
  @Data
  public static class EndpointLimit {
    /** Numero maximo de requisicoes permitidas na janela de tempo */
    private int requests;

    /** Duracao da janela de tempo em minutos */
    private int durationMinutes;

    public EndpointLimit() {
      this.requests = 10;
      this.durationMinutes = 15;
    }

    public EndpointLimit(int requests, int durationMinutes) {
      this.requests = requests;
      this.durationMinutes = durationMinutes;
    }
  }
}
