package br.com.utfpr.gerenciamento.server.config;

import java.util.concurrent.Executor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** Configuração de teste para desabilitar processamento async de emails. */
@TestConfiguration
@EnableAsync
public class TestAsyncConfig {

  @Bean
  @Primary
  public Executor emailTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(0);
    executor.setMaxPoolSize(0);
    executor.setQueueCapacity(0);
    executor.setThreadNamePrefix("test-email-");
    executor.setRejectedExecutionHandler(
        (r, executor1) -> {
          // Silently reject email tasks in tests
        });
    executor.initialize();
    return executor;
  }
}
