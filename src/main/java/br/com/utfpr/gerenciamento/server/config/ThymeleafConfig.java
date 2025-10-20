package br.com.utfpr.gerenciamento.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
public class ThymeleafConfig {
  @Bean
  public TemplateEngine thymeleafTemplateEngine() {
    ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
    templateResolver.setPrefix("templates/");
    templateResolver.setSuffix(".html");
    templateResolver.setTemplateMode("HTML");
    templateResolver.setCharacterEncoding("UTF-8");
    templateResolver.setCacheable(false);
    TemplateEngine engine = new TemplateEngine();
    engine.setTemplateResolver(templateResolver);
    return engine;
  }
}
