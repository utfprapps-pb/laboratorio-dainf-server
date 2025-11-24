package br.com.utfpr.gerenciamento.server.config;

import java.util.concurrent.Executor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;

/** Configuração de teste para desabilitar processamento async de emails. */
@TestConfiguration
@EnableAsync
public class TestAsyncConfig {

  @Bean
  @Primary
  public Executor emailTaskExecutor() {
    return Runnable::run;
  }
}
