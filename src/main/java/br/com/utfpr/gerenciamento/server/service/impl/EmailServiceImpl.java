package br.com.utfpr.gerenciamento.server.service.impl;

import br.com.utfpr.gerenciamento.server.model.Email;
import br.com.utfpr.gerenciamento.server.service.EmailService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.internet.MimeMessage;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSenderImpl javaMailSender;
    private final Configuration freemarkerConfiguration;

    @Autowired
    public EmailServiceImpl(JavaMailSenderImpl javaMailSender,
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

                if (email.getPara() != null && !email.getPara().equals("")) {
                    helper.setBcc(email.getPara());
                } else if (email.getParaList() != null && email.getParaList().size() > 0) {
                    helper.setBcc(email.getParaList().toArray(new String[0]));
                } else {
                    throw new Exception("Nenhum email encontrado para envio.");
                }

                helper.setSubject(email.getTitulo());
                helper.setText(email.getConteudo(), true);

                for (Map.Entry<String, byte[]> entry : email.getFileMap().entrySet()) {
                    helper.addAttachment(entry.getKey(), new ByteArrayResource(entry.getValue()));
                }

                javaMailSender.setUsername("dainf.labs@gmail.com");
                javaMailSender.setPassword("tcc-mail@2020");
                javaMailSender.send(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public String buildTemplateEmail(Object object, String nameTemplate) {
        Template template;
        try {
            template = freemarkerConfiguration.getTemplate(String.format("%s.ftl", nameTemplate));
            String templateEmail = FreeMarkerTemplateUtils.processTemplateIntoString(template, object);
            return templateEmail;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void sendEmailWithTemplate(Object objectTemplate, String to, String titleEmail, String nameTemplate) {
        Email email = new Email()
                .setPara(to)
                .setDe("dainf.labs@gmail.com")
                .setTitulo(titleEmail)
                .setConteudo(this.buildTemplateEmail(objectTemplate, nameTemplate));
        try {
            this.enviar(email);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
