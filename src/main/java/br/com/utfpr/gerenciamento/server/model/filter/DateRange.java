package br.com.utfpr.gerenciamento.server.model.filter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

/** Value object representando um intervalo de datas. */
public record DateRange(LocalDate inicio, LocalDate fim) {

  /**
   * Construtor com validação de range.
   *
   * @param inicio Data de início (pode ser null)
   * @param fim Data de fim (pode ser null)
   * @throws IllegalArgumentException se fim < inicio ou se datas são iguais e representam range
   *     vazio
   */
  @JsonCreator
  public DateRange(@JsonProperty("inicio") LocalDate inicio, @JsonProperty("fim") LocalDate fim) {
    if (inicio != null && fim != null && fim.isBefore(inicio)) {
      throw new IllegalArgumentException(
          String.format("Data fim (%s) não pode ser anterior à data início (%s)", fim, inicio));
    }
    this.inicio = inicio;
    this.fim = fim;
  }

  /**
   * Cria DateRange a partir de Strings ISO (yyyy-MM-dd).
   *
   * @param inicioStr String data início (pode ser null/empty)
   * @param fimStr String data fim (pode ser null/empty)
   * @return DateRange ou null se ambos parâmetros são null/empty
   * @throws IllegalArgumentException se range inválido
   */
  public static DateRange fromStrings(String inicioStr, String fimStr) {
    LocalDate inicio =
        (inicioStr != null && !inicioStr.isEmpty()) ? LocalDate.parse(inicioStr) : null;
    LocalDate fim = (fimStr != null && !fimStr.isEmpty()) ? LocalDate.parse(fimStr) : null;

    if (inicio == null && fim == null) {
      return null;
    }

    return new DateRange(inicio, fim);
  }

  public boolean hasInicio() {
    return inicio != null;
  }

  public boolean hasFim() {
    return fim != null;
  }

  public boolean isComplete() {
    return inicio != null && fim != null;
  }

  /**
   * Verifica se uma data está dentro do range.
   *
   * @param date Data a verificar
   * @return true se date está no intervalo [inicio, fim]
   */
  public boolean contains(LocalDate date) {
    if (date == null) {
      return false;
    }

    boolean afterStart = inicio == null || !date.isBefore(inicio);
    boolean beforeEnd = fim == null || !date.isAfter(fim);

    return afterStart && beforeEnd;
  }

  @Override
  public String toString() {
    if (inicio == null && fim == null) {
      return "DateRange[]";
    } else if (inicio == null) {
      return String.format("DateRange[até %s]", fim);
    } else if (fim == null) {
      return String.format("DateRange[desde %s]", inicio);
    } else {
      return String.format("DateRange[%s até %s]", inicio, fim);
    }
  }
}
