package br.com.utfpr.gerenciamento.server.security;

import org.springframework.security.core.AuthenticationException;

/** Exception to indicate a precondition is required for authentication (HTTP 428). */
public class PreconditionRequiredAuthenticationException extends AuthenticationException {
  public PreconditionRequiredAuthenticationException(String msg) {
    super(msg);
  }
}
