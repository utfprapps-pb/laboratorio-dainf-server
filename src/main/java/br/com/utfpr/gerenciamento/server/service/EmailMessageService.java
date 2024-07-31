package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.dto.EmailDto;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import java.util.Arrays;


@Service
@Data
@Slf4j
public class EmailMessageService {
    @Value("${utfpr.email.address}")
    private String emailAddress;
    @Value("${utfpr.email.username}")
    private String emailUsername;
    @Value("${utfpr.email.password}")
    private String emailPassword;
    @Value("${utfpr.email.host}")
    private String emailHost;
    @Value("${utfpr.email.port}")
    private String emailPort;

    private final EmailMessageGenerator emailMessageGenerator;

    public EmailMessageService(EmailMessageGenerator emailMessageGenerator) {
        this.emailMessageGenerator = emailMessageGenerator;
    }

    public void sendEmail(EmailDto email, String template) {
        new Thread(() -> {
            HtmlEmail htmlEmail = new HtmlEmail();
            try {
                configEmail(htmlEmail);
                htmlEmail.setSubject(email.getSubject());
                htmlEmail.addTo(email.getEmailTo());
                htmlEmail.setHtmlMsg(emailMessageGenerator.generateHTML(email, template));
                log.info("Sending email to " + email.getEmailTo());
                htmlEmail.send();
                log.info("Email was sent.");
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Error sending email. " + e.getMessage() + Arrays.toString(e.getStackTrace()));
            }
        }).start();
    }

    private void configEmail(HtmlEmail htmlEmail) throws EmailException {
        htmlEmail.setHostName(emailHost);
        htmlEmail.setSmtpPort(Integer.parseInt(emailPort));
        htmlEmail.setSslSmtpPort(emailPort);

        htmlEmail.setAuthenticator(new DefaultAuthenticator(emailAddress, emailPassword));
        htmlEmail.setSSLOnConnect(true);
        htmlEmail.setFrom(emailAddress);
        htmlEmail.setStartTLSRequired(true);
    }
}
