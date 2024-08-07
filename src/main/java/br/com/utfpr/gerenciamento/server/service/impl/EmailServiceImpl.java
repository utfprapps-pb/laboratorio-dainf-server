package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Email;
import br.com.utfpr.gerenciamento.server.service.EmailService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import jakarta.mail.internet.MimeMessage;
import java.util.Map;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Value("${utfpr.email.address}")
    private String emailAddress;

    private final JavaMailSender javaMailSender;
    private final Configuration freemarkerConfiguration;

    @Autowired
    public EmailServiceImpl(JavaMailSender javaMailSender,
                            Configuration freemarkerConfiguration) {
        this.javaMailSender = javaMailSender;
        this.freemarkerConfiguration = freemarkerConfiguration;
    }

    @Override
    public void enviar(Email email) throws Exception {
        new Thread(() -> {
            try {
                MimeMessage message = javaMailSender.createMimeMessage();

                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(email.getDe(), "Laboratório de Informática - UTFPR/PB");
                helper.setReplyTo(email.getDe());

                if (email.getPara() != null && !email.getPara().isEmpty()) {
                    helper.setBcc(email.getPara());
                } else if (email.getParaList() != null && !email.getParaList().isEmpty()) {
                    helper.setBcc(email.getParaList().toArray(new String[0]));
                } else {
                    throw new Exception("Nenhum email encontrado para envio.");
                }

                helper.setSubject(email.getTitulo());
                helper.setText(email.getConteudo(), true);

                if (email.getFileMap() != null && !email.getFileMap().isEmpty()) {
                    for (Map.Entry<String, byte[]> entry : email.getFileMap().entrySet()) {
                        helper.addAttachment(entry.getKey(), new ByteArrayResource(entry.getValue()));
                    }
                }

                log.info("Sending email....");
                javaMailSender.send(message);
                log.info("Email sent.");
            } catch (Exception ex) {
                log.error("Error sending email. ", ex);
            }
        }).start();
    }

    @Override
    public String buildTemplateEmail(Object object, String nameTemplate) {
        Template template;
        try {
            template = freemarkerConfiguration.getTemplate(String.format("%s.ftl", nameTemplate));
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, object);
        } catch (Exception ex) {
            log.error("Building email template. ", ex);
            return null;
        }
    }

    @Override
    public void sendEmailWithTemplate(Object objectTemplate, String to, String titleEmail, String nameTemplate) {
        Email email = Email.builder()
                .para(to)
                .de(emailAddress)
                .titulo(titleEmail)
                .conteudo(this.buildTemplateEmail(objectTemplate, nameTemplate)).build();
        try {
            this.enviar(email);
        } catch (Exception ex) {
            log.error("Error sending email. ", ex);
        }
    }
}
