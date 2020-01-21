package br.com.utfpr.gerenciamento.server.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class LocalDateDeserializer extends JsonDeserializer<LocalDate> {

    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return toLocalDate(p.getValueAsString());
    }

    public LocalDate toLocalDate(String value) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(value, formatter);
        } catch (DateTimeParseException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }
}