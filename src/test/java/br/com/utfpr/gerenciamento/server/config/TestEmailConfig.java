package br.com.utfpr.gerenciamento.server.config;

import java.util.Properties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/** Configuração de teste para desabilitar envio de emails. */
@TestConfiguration
public class TestEmailConfig {

  @Bean
  @Primary
  public JavaMailSender testMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

    // Configurações dummy para não tentar conectar ao SMTP real
    mailSender.setHost("localhost");
    mailSender.setPort(3025); // porta fake
    mailSender.setUsername("test@test.com");
    mailSender.setPassword("test");

    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.auth", "false");
    props.put("mail.smtp.starttls.enable", "false");
    props.put("mail.debug", "false");
    props.put("mail.smtp.connectiontimeout", "1");
    props.put("mail.smtp.timeout", "1");
    props.put("mail.smtp.writetimeout", "1");

    return mailSender;
  }
}
