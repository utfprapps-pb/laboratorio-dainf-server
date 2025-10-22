package br.com.utfpr.gerenciamento.server.config;

import br.com.utfpr.gerenciamento.server.util.DateUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Deserializer Jackson para LocalDate no formato brasileiro dd/MM/yyyy.
 *
 * <p>Usa {@link DateUtil#BR_DATE_FORMATTER} para garantir parsing consistente com locale pt-BR.
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
   * Converte String no formato dd/MM/yyyy para LocalDate.
   *
   * @param value String da data
   * @return LocalDate parseado, ou null em caso de erro
   */
  public LocalDate toLocalDate(String value) {
    try {
      return LocalDate.parse(value, DateUtil.BR_DATE_FORMATTER);
    } catch (DateTimeParseException ex) {
      log.error("Erro ao desserializar data '{}': {}", value, ex.getMessage(), ex);
      return null;
    }
  }
}
