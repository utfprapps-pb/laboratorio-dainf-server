package br.com.utfpr.gerenciamento.server.enumeration;

import lombok.Getter;

/**
 * Enum representando as roles (permissões) de usuário no sistema.
 *
 * <p>Os valores correspondem aos nomes armazenados na tabela 'permissao'. Spring Security
 * automaticamente adiciona o prefixo "ROLE_" ao verificar permissões, mas o valor armazenado no
 * banco de dados já inclui esse prefixo.
 */
@Getter
public enum UserRole {
  /** Administrador do sistema - acesso total */
  ADMINISTRADOR("ROLE_ADMINISTRADOR"),

  /** Laboratorista - gerenciamento de laboratórios e equipamentos */
  LABORATORISTA("ROLE_LABORATORISTA"),

  /** Usuário comum - acesso básico ao sistema */
  USUARIO("ROLE_USUARIO"),

  /** Professor - acesso acadêmico com privilégios de docente */
  PROFESSOR("ROLE_PROFESSOR"),

  /** Aluno - acesso acadêmico básico para estudantes */
  ALUNO("ROLE_ALUNO");

  // Constantes para uso em anotações (compile-time constants)
  public static final String ROLE_ADMINISTRADOR_NAME = "ADMINISTRADOR";
  public static final String ROLE_LABORATORISTA_NAME = "LABORATORISTA";
  public static final String ROLE_PROFESSOR_NAME = "PROFESSOR";
  public static final String ROLE_ALUNO_NAME = "ALUNO";

  private final String authority;

  UserRole(String authority) {
    this.authority = authority;
  }
}
