package br.com.utfpr.gerenciamento.server.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuração de processamento assíncrono e retry para email.
 *
 * <p>Habilita @Async para envio não-bloqueante de emails após eventos transacionais e @Retryable
 * para retry automático em caso de falhas transientes.
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-22
 */
@Slf4j
@Configuration
@EnableAsync
@EnableRetry
public class AsyncConfig implements AsyncConfigurer {

  /**
   * Thread pool dedicado para processamento assíncrono de emails.
   *
   * <p>Configuração: - Core threads: 5 (mínimo sempre ativo) - Max threads: 20 (pico de carga) -
   * Queue capacity: 100 (buffer para rajadas) - Rejection policy: CallerRunsPolicy (backpressure
   * controlado)
   *
   * @return Executor configurado para emails
   */
  @Bean(name = "emailTaskExecutor")
  public Executor emailTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    // Threads mínimas e máximas
    executor.setCorePoolSize(5); // Threads sempre ativas
    executor.setMaxPoolSize(20); // Máximo sob carga
    executor.setQueueCapacity(100); // Buffer para rajadas

    // Nome das threads para debugging
    executor.setThreadNamePrefix("email-async-");

    // Política de rejeição: volta para thread original (backpressure)
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

    // Garante que tasks em execução completem no shutdown
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(60); // Aguarda até 60s

    executor.initialize();
    return executor;
  }

  /**
   * Handler para exceções não capturadas em métodos @Async.
   *
   * <p>Loga erros que ocorrem em processamento assíncrono mas não propaga exceção (não afeta thread
   * original).
   */
  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return (ex, method, params) -> {
      String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
      log.error("Erro assíncrono não capturado em {}", methodName, ex);
      // TODO: Integrar com sistema de alertas para notificar ops team
    };
  }
}
