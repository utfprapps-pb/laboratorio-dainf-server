package br.com.utfpr.gerenciamento.server.enumeration;

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
