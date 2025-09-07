package br.com.utfpr.gerenciamento.server.ennumeation;

public enum TypeParamReport {
  D("Data"),
  S("String"),
  N("Numero");

  private String label;

  TypeParamReport(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
