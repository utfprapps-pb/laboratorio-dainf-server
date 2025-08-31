package br.com.utfpr.gerenciamento.server.config;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

  @Value("${utfpr.email.host}")
  private String host;

  @Value("${utfpr.email.port}")
  private Integer port;

  @Value("${utfpr.email.address}")
  private String username;

  @Value("${utfpr.email.password}")
  private String password;

  @Bean
  public JavaMailSender getJavaMailSender() {
    JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
    javaMailSender.setUsername(username);
    javaMailSender.setPassword(password);
    javaMailSender.setHost(host);
    javaMailSender.setPort(port);
    javaMailSender.setProtocol("smtp");
    javaMailSender.setDefaultEncoding("UTF-8");
    javaMailSender.setJavaMailProperties(getMailProperties());
    return javaMailSender;
  }

  private Properties getMailProperties() {
    Properties properties = new Properties();
    properties.setProperty("mail.transport.protocol", "smtp");
    properties.setProperty("mail.smtp.auth", "true");
    properties.setProperty("mail.smtp.starttls.enable", "true");
    properties.setProperty("mail.debug", "false");

    // properties.setProperty("mail.smtp.starttls.required", "true");
    properties.setProperty("mail.smtp.ssl.trust", host);
    //        <prop key="mail.smtp.ssl.protocols">TLSv1.2</prop>
    properties.setProperty("mail.smtp.timeout", "360000");

    return properties;
  }
}
