package br.com.utfpr.gerenciamento.server.event.usuario;

import br.com.utfpr.gerenciamento.server.event.email.EmailEvent;
import lombok.Getter;

/**
 * Evento publicado quando um novo usuário é criado no sistema.
 *
 * <p>Dispara envio de email de confirmação de cadastro usando template templateConfirmacaoCadastro.
 *
 * <p>Este evento garante que:
 *
 * <ul>
 *   <li>Email só é enviado se a transação de criação do usuário commit com sucesso
 *   <li>Falha no envio de email não causa rollback da criação do usuário
 *   <li>Usuário pode reenviar email de confirmação posteriormente se falhar
 * </ul>
 *
 * @author Rodrigo Izidoro
 * @since 2025-10-27
 */
@Getter
public class UsuarioCriadoEvent extends EmailEvent {

  private final Long usuarioId;
  private final String codigoVerificacao;

  /**
   * Cria evento de usuário criado.
   *
   * @param source Service que publicou o evento
   * @param usuarioId ID do usuário criado
   * @param recipient Email do usuário
   * @param codigoVerificacao Código de verificação único do usuário
   */
  public UsuarioCriadoEvent(
      Object source, Long usuarioId, String recipient, String codigoVerificacao) {
    super(
        source,
        recipient,
        "Confirmação de email - Laboratório DAINF-PB (UTFPR)",
        "templateConfirmacaoCadastro");
    this.usuarioId = usuarioId;
    this.codigoVerificacao = codigoVerificacao;
  }
}
