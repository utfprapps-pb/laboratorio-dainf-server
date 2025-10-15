package br.com.utfpr.gerenciamento.server.config;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.stream.StreamSupport;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Deserializador customizado para GrantedAuthority do Spring Security.
 *
 * <p>Converte array JSON de authorities para List<GrantedAuthority>. Modernizado com Java 21
 * Streams API e type-safe generics.
 */
public class CustomAuthorityDeserializer extends JsonDeserializer<List<GrantedAuthority>> {

  @Override
  public List<GrantedAuthority> deserialize(
      com.fasterxml.jackson.core.JsonParser jp, DeserializationContext ctx) throws IOException {
    var mapper = (ObjectMapper) jp.getCodec();
    JsonNode jsonNode = mapper.readTree(jp);

    return StreamSupport.stream(jsonNode.spliterator(), false)
        .map(node -> (GrantedAuthority) new SimpleGrantedAuthority(node.get("authority").asText()))
        .toList();
  }
}
