package br.com.utfpr.gerenciamento.server.model.modelTemplateEmail;

import br.com.utfpr.gerenciamento.server.model.ReservaItem;
import lombok.Data;

import java.util.List;

@Data
public class ReservaTemplate {

    private String usuario;
    private String dtReserva;
    private String dtRetirada;
    private List<ReservaItem> reservaItem;
}
