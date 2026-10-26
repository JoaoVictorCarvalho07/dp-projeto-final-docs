package br.pucpr.docs.model;

import br.pucpr.docs.visitor.DocumentVisitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** COMPOSITE: nó composto. Uma seção contém outros nós (parágrafos, tabelas, subseções). */
public final class Section implements Node {
  private String title;
  private final List<Node> children = new ArrayList<>();

  public Section(String title) {
    this.title = title;
  }

  public Section add(Node child) {
    children.add(child);
    return this;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public List<Node> getChildren() {
    return Collections.unmodifiableList(children);
  }

  @Override
  public void accept(DocumentVisitor visitor) {
    visitor.visitSection(this);
  }

  /** PROTOTYPE: cópia profunda, clona cada filho. */
  @Override
  public Section copy() {
    Section clone = new Section(title);
    for (Node child : children) {
      clone.add(child.copy());
    }
    return clone;
  }
}
