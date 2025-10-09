package br.com.utfpr.gerenciamento.server.enumeration;

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
