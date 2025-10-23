package br.com.utfpr.gerenciamento.server.security;

/**
 * Constantes centralizadas para rotas da API.
 *
 * <p>Centraliza os padrões de URL usados na configuração de segurança para evitar duplicação de
 * strings mágicas e facilitar manutenção.
 */
public final class ApiRoutes {

  // Prevent instantiation
  private ApiRoutes() {
    throw new UnsupportedOperationException("Utility class");
  }

  // ============ Endpoints Administrativos ============
  /** Gerenciamento de cidades */
  public static final String CIDADE = "/cidade/**";

  /** Gerenciamento de estados */
  public static final String ESTADO = "/estado/**";

  /** Gerenciamento de países */
  public static final String PAIS = "/pais/**";

  /** Relatórios do sistema */
  public static final String RELATORIO = "/relatorio/**";

  /** Gerenciamento de fornecedores */
  public static final String FORNECEDOR = "/fornecedor/**";

  /** Gerenciamento de compras */
  public static final String COMPRA = "/compra/**";

  /** Controle de entrada de itens */
  public static final String ENTRADA = "/entrada/**";

  /** Gerenciamento de grupos */
  public static final String GRUPO = "/grupo/**";

  /** Controle de saída de itens */
  public static final String SAIDA = "/saida/**";

  // ============ Item Endpoints ============
  /** Gerenciamento de itens (equipamentos) */
  public static final String ITEM = "/item/**";

  // ============ Usuário Endpoints ============
  /** Base para todos os endpoints de usuário */
  public static final String USUARIO = "/usuario/**";

  /** Registro de novo usuário */
  public static final String USUARIO_NEW_USER = "/usuario/new-user/**";

  /** Reenvio de email de confirmação */
  public static final String USUARIO_RESEND_CONFIRM = "/usuario/resend-confirm-email/**";

  /** Confirmação de email */
  public static final String USUARIO_CONFIRM_EMAIL = "/usuario/confirm-email/**";

  /** Reset de senha */
  public static final String USUARIO_RESET_PASSWORD = "/usuario/reset-password/**";

  /** Solicitação de código para reset de senha */
  public static final String USUARIO_REQUEST_CODE_RESET = "/usuario/request-code-reset-password/**";

  /** Atualização de dados do usuário autenticado */
  public static final String USUARIO_UPDATE = "/usuario/update-user";

  /** Informações do usuário autenticado */
  public static final String USUARIO_INFO = "/usuario/user-info";

  /** Busca de usuário por username */
  public static final String USUARIO_FIND_BY_USERNAME = "/usuario/find-by-username/**";

  // ============ Empréstimo Endpoints ============
  /** Base para todos os endpoints de empréstimo */
  public static final String EMPRESTIMO = "/emprestimo/**";

  /** Salvar novo empréstimo */
  public static final String EMPRESTIMO_SAVE = "/emprestimo/save-emprestimo";

  /** Salvar devolução */
  public static final String EMPRESTIMO_DEVOLUCAO = "/emprestimo/save-devolucao";

  // ============ Endpoints Públicos ============
  /** Autenticação (login) */
  public static final String AUTH = "/auth";

  /** Endpoint de teste */
  public static final String TEST = "/test";

  // ============ Actuator (Monitoramento) ============
  /** Endpoints de monitoramento Spring Actuator */
  public static final String ACTUATOR = "/actuator/**";

  /** Config endpoints */
  public static final String CONFIG = "/config";

  // ============ Nada Consta Endpoints ============
  /** Base para todos os endpoints de nada consta */
  public static final String NADACONSTA = "/nadaconsta/**";

  /** Endpoint de solicitação de nada consta */
  public static final String NADACONSTA_SOLICITAR = "/nadaconsta/solicitar";
}
