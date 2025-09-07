package br.com.utfpr.gerenciamento.server.ennumeation;

public enum TipoItem {
  C("Consumo"),
  P("Permanente");

  private String label;

  TipoItem(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
