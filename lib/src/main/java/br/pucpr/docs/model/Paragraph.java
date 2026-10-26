package br.pucpr.docs.model;

import br.pucpr.docs.visitor.DocumentVisitor;

/** COMPOSITE: folha com texto. */
public final class Paragraph implements Node {
  private String text;

  public Paragraph(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  @Override
  public void accept(DocumentVisitor visitor) {
    visitor.visitParagraph(this);
  }

  @Override
  public Paragraph copy() {
    return new Paragraph(text);
  }
}
