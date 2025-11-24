package br.com.utfpr.gerenciamento.server.config;

import br.com.utfpr.gerenciamento.server.util.DateUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Deserializer Jackson para LocalDate que suporta múltiplos formatos.
 *
 * <p>Aceita tanto o formato brasileiro dd/MM/yyyy quanto o formato ISO yyyy-MM-dd.
 *
 * <p>Tenta primeiro o formato brasileiro {@link DateUtil#BR_DATE_FORMATTER}, e se falhar, tenta o
 * formato ISO padrão.
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-22
 */
@Slf4j
@Configuration
public class LocalDateDeserializer extends JsonDeserializer<LocalDate> {

  @Override
  public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    return toLocalDate(p.getValueAsString());
  }

  /**
   * Converte String para LocalDate, suportando múltiplos formatos.
   *
   * <p>Formatos suportados:
   *
   * <ul>
   *   <li>dd/MM/yyyy (formato brasileiro)
   *   <li>yyyy-MM-dd (formato ISO)
   * </ul>
   *
   * @param value String da data
   * @return LocalDate parseado, ou null em caso de erro
   */
  public LocalDate toLocalDate(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }

    // Tenta primeiro o formato brasileiro dd/MM/yyyy
    try {
      return LocalDate.parse(value, DateUtil.BR_DATE_FORMATTER);
    } catch (DateTimeParseException ex) {
      log.debug("Não foi possível parsear '{}' como formato brasileiro, tentando ISO", value);
    }

    // Tenta o formato ISO yyyy-MM-dd
    try {
      return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
    } catch (DateTimeParseException ex) {
      log.error(
          "Erro ao desserializar data '{}': formato inválido. Formatos aceitos: dd/MM/yyyy ou yyyy-MM-dd",
          value,
          ex);
      return null;
    }
  }
}
