package br.com.utfpr.gerenciamento.server.enumeration;

/**
 * Enum representando as roles (permissões) de usuário no sistema.
 *
 * <p>Os valores correspondem aos nomes armazenados na tabela 'permissao'. Spring Security
 * automaticamente adiciona o prefixo "ROLE_" ao verificar permissões, mas o valor armazenado no
 * banco de dados já inclui esse prefixo.
 */
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
  public static final String ROLE_USUARIO_NAME = "USUARIO";
  public static final String ROLE_PROFESSOR_NAME = "PROFESSOR";
  public static final String ROLE_ALUNO_NAME = "ALUNO";

  private final String authority;

  UserRole(String authority) {
    this.authority = authority;
  }

  /**
   * Retorna o nome da authority como armazenado no banco de dados.
   *
   * @return String com prefixo "ROLE_" (ex: "ROLE_ADMINISTRADOR")
   */
  public String getAuthority() {
    return authority;
  }

  /**
   * Retorna o nome da role SEM o prefixo "ROLE_" para uso com Spring Security annotations.
   *
   * <p>Spring Security automaticamente adiciona "ROLE_" ao verificar com hasRole() ou hasAnyRole().
   *
   * @return String sem prefixo "ROLE_" (ex: "ADMINISTRADOR")
   */
  public String getRoleName() {
    return authority.substring(5); // Remove "ROLE_" prefix
  }

  /**
   * Retorna array de role names para uso em hasAnyRole().
   *
   * @param roles varargs de UserRole
   * @return array de Strings sem prefixo "ROLE_"
   */
  public static String[] toRoleNames(UserRole... roles) {
    String[] names = new String[roles.length];
    for (int i = 0; i < roles.length; i++) {
      names[i] = roles[i].getRoleName();
    }
    return names;
  }

  /**
   * Encontra o enum UserRole correspondente a uma authority string.
   *
   * <p>Útil para converter valores do banco de dados de volta para enum.
   *
   * @param authority String com prefixo "ROLE_" (ex: "ROLE_ADMINISTRADOR")
   * @return UserRole correspondente
   * @throws IllegalArgumentException se a authority não for reconhecida
   */
  public static UserRole fromAuthority(String authority) {
    for (UserRole role : values()) {
      if (role.authority.equals(authority)) {
        return role;
      }
    }
    throw new IllegalArgumentException("Role desconhecida: " + authority);
  }
}
