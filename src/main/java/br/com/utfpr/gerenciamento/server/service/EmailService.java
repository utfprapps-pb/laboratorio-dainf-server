package br.com.utfpr.gerenciamento.server.service;

import br.com.utfpr.gerenciamento.server.model.Email;

public interface EmailService {

  void enviar(Email email) throws Exception;

  String buildTemplateEmail(Object object, String nameTemplate);

  void sendEmailWithTemplate(
      Object objectTemplate, String to, String titleEmail, String nameTemplate);
}
