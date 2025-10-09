package br.com.utfpr.gerenciamento.server.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.pattern.PathPatternParser;

/** Configuração web da aplicação. Define beans e configurações globais para o contexto MVC. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  /**
   * Cria e configura o ModelMapper para conversão entre entidades e DTOs.
   *
   * @return instância configurada do ModelMapper
   */
  @Bean
  public ModelMapper modelMapper() {
    return new ModelMapper();
  }

  /**
   * Configura o matching de paths para manter compatibilidade com trailing slashes. Permite que
   * endpoints com e sem barra final sejam tratados como equivalentes. Exemplo: /api/usuarios e
   * /api/usuarios/ são considerados o mesmo endpoint.
   *
   * <p>NOTA: Esta configuração mantém o comportamento legado para compatibilidade. Em Spring Boot
   * 3.5.x, o metodo {@code setPatternParser()} está deprecado. A recomendação futura é remover esta
   * configuração e ajustar os endpoints para seguir o padrão REST (URLs sem trailing slashes).
   *
   * <p>TODO: Avaliar impacto no frontend e nos consumidores da API antes de remover.
   *
   * @param configurer configurador de matching de paths
   */
  @SuppressWarnings("deprecation") // Mantido para compatibilidade legada
  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    PathPatternParser patternParser = new PathPatternParser();
    patternParser.setMatchOptionalTrailingSeparator(true);
    configurer.setPatternParser(patternParser);
  }
}
