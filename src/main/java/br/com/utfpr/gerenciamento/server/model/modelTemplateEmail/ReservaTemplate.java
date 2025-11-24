package br.com.utfpr.gerenciamento.server.model.modelTemplateEmail;

import br.com.utfpr.gerenciamento.server.model.ReservaItem;
import java.util.List;
import lombok.Data;

@Data
public class ReservaTemplate {

  private String usuario;
  private String dtReserva;
  private String dtRetirada;
  private List<ReservaItem> reservaItem;
}
