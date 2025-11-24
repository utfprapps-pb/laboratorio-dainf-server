package br.com.utfpr.gerenciamento.server.config;

import br.com.utfpr.gerenciamento.server.util.DateUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;

/**
 * Serializer Jackson para LocalDate no formato brasileiro dd/MM/yyyy.
 *
 * <p>Usa {@link DateUtil#BR_DATE_FORMATTER} para garantir formato consistente com locale pt-BR.
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-22
 */
@Slf4j
public class LocalDateSerializer extends JsonSerializer<LocalDate> {

  @Override
  public void serialize(LocalDate value, JsonGenerator jsonGenerator, SerializerProvider serializer)
      throws IOException {
    jsonGenerator.writeString(toString(value));
  }

  /**
   * Converte LocalDate para String no formato dd/MM/yyyy.
   *
   * @param value LocalDate a ser formatado
   * @return String da data formatada, ou null em caso de erro
   */
  public String toString(LocalDate value) {
    try {
      return value.format(DateUtil.BR_DATE_FORMATTER);
    } catch (Exception ex) {
      log.error("Erro ao serializar LocalDate: {}", ex.getMessage(), ex);
      return null;
    }
  }
}
