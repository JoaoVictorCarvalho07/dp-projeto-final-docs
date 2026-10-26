package br.pucpr.docs.model;

import br.pucpr.docs.visitor.DocumentVisitor;
import java.util.ArrayList;
import java.util.List;

/** COMPOSITE: folha com cabeçalho e linhas. */
public final class Table implements Node {
  private final List<String> headers;
  private final List<List<String>> rows = new ArrayList<>();

  public Table(List<String> headers) {
    this.headers = List.copyOf(headers);
  }

  public Table addRow(List<String> row) {
    rows.add(List.copyOf(row));
    return this;
  }

  public List<String> getHeaders() {
    return headers;
  }

  public List<List<String>> getRows() {
    return List.copyOf(rows);
  }

  @Override
  public void accept(DocumentVisitor visitor) {
    visitor.visitTable(this);
  }

  @Override
  public Table copy() {
    Table clone = new Table(headers);
    rows.forEach(clone::addRow);
    return clone;
  }
}
