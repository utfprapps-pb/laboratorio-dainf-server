package br.com.utfpr.gerenciamento.server.ennumeation;

public enum StatusDevolucao {
    P("Pendente"),
    D("Devolvido"),
    S("Saida");

    private String label;

    StatusDevolucao(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
