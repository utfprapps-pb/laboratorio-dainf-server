package br.com.utfpr.gerenciamento.server.enumeration;

/**
 * Status do empréstimo baseado em datas de devolução e prazo.
 *
 * <p>Este enum substitui códigos String ("A", "P", "F", "T") por constantes type-safe com semântica
 * clara.
 *
 * <p>Regras de negócio: - ATRASADO: Sem data de devolução e prazo vencido - EM_ANDAMENTO: Sem data
 * de devolução e prazo não vencido - FINALIZADO: Com data de devolução registrada - TODOS: Filtro
 * especial para buscar todos os status
 */
public enum EmprestimoStatus {
  /** Empréstimo atrasado (sem devolução, prazo vencido) */
  ATRASADO("A", "Atrasado"),

  /** Empréstimo em andamento (sem devolução, prazo não vencido) */
  EM_ANDAMENTO("P", "Em andamento"),

  /** Empréstimo finalizado (com data de devolução) */
  FINALIZADO("F", "Finalizado"),

  /** Filtro especial: todos os status (usado apenas em filtros) */
  TODOS("T", "Todos");

  private final String codigo;
  private final String descricao;

  EmprestimoStatus(String codigo, String descricao) {
    this.codigo = codigo;
    this.descricao = descricao;
  }

  public String getCodigo() {
    return codigo;
  }

  public String getDescricao() {
    return descricao;
  }

  /**
   * Converte código String para enum.
   *
   * @param codigo Código de uma letra ("A", "P", "F", "T")
   * @return Enum correspondente
   * @throws IllegalArgumentException se código inválido
   */
  public static EmprestimoStatus fromCodigo(String codigo) {
    if (codigo == null) {
      return null;
    }

    for (EmprestimoStatus status : values()) {
      if (status.codigo.equals(codigo)) {
        return status;
      }
    }

    throw new IllegalArgumentException("Código de status inválido: " + codigo);
  }

  /**
   * Converte código String para enum, retornando null se inválido.
   *
   * @param codigo Código de uma letra ("A", "P", "F", "T")
   * @return Enum correspondente ou null se inválido
   */
  public static EmprestimoStatus fromCodigoOrNull(String codigo) {
    try {
      return fromCodigo(codigo);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  @Override
  public String toString() {
    return descricao;
  }
}
