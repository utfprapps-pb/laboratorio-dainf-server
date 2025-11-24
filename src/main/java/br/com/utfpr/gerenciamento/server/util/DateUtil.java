package br.com.utfpr.gerenciamento.server.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utilitários para manipulação de datas no padrão brasileiro (pt-BR).
 *
 * <p>Fornece constantes e métodos para formatação e parsing de datas no formato dd/MM/yyyy com
 * locale pt-BR, garantindo comportamento consistente independente da configuração do ambiente.
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-22
 */
public final class DateUtil {

  /** Locale brasileiro (pt-BR) para formatação de datas. */
  public static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

  /** Formatador de datas no padrão brasileiro: dd/MM/yyyy com locale pt-BR. */
  public static final DateTimeFormatter BR_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(PT_BR);

  private DateUtil() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Converte String no formato dd/MM/yyyy para LocalDate.
   *
   * @param dt String da data no formato dd/MM/yyyy
   * @return LocalDate parseado
   * @throws java.time.format.DateTimeParseException se a string não estiver no formato esperado
   */
  public static LocalDate parseStringToLocalDate(String dt) {
    return LocalDate.parse(dt, BR_DATE_FORMATTER);
  }

  /**
   * Converte LocalDate para String no formato dd/MM/yyyy.
   *
   * @param dt LocalDate a ser formatado
   * @return String da data no formato dd/MM/yyyy
   */
  public static String parseLocalDateToString(LocalDate dt) {
    return dt.format(BR_DATE_FORMATTER);
  }
}
