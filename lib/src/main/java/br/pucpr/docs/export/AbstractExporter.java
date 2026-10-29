package br.pucpr.docs.export;

import br.pucpr.docs.model.Document;
import br.pucpr.docs.model.Node;
import br.pucpr.docs.model.Section;
import br.pucpr.docs.theme.ClassicThemeFactory;
import br.pucpr.docs.theme.ThemeFactory;
import br.pucpr.docs.visitor.DocumentVisitor;

/**
 * TEMPLATE METHOD: {@link #export(Document)} define o fluxo fixo (cabeçalho → corpo → rodapé). As
 * subclasses só preenchem os passos. Também é um VISITOR: o corpo é gerado visitando a árvore.
 *
 * <p>Pra criar um formato novo, estenda esta classe fora da lib. Nada aqui muda (Open/Closed).
 */
public abstract class AbstractExporter implements DocumentExporter, DocumentVisitor {
  protected final ThemeFactory theme;
  protected StringBuilder out;
  protected int depth;
  protected RowHighlighter highlighter = RowHighlighter.NONE;

  protected AbstractExporter(ThemeFactory theme) {
    this.theme = theme;
  }

  protected AbstractExporter() {
    this(new ClassicThemeFactory());
  }

  /** Liga o destaque de linhas de tabela (Strategy). Devolve o próprio exportador. */
  public AbstractExporter highlightRows(RowHighlighter highlighter) {
    this.highlighter = highlighter == null ? RowHighlighter.NONE : highlighter;
    return this;
  }

  /** O template method: final pra ninguém mudar a ordem dos passos. */
  @Override
  public final String export(Document document) {
    out = new StringBuilder();
    depth = 0;
    writeHeader(document);
    writeBody(document);
    writeFooter(document);
    return out.toString();
  }

  protected abstract void writeHeader(Document document);

  /** Passo padrão: visita os filhos da raiz. Pode ser sobrescrito. */
  protected void writeBody(Document document) {
    for (Node child : document.getRoot().getChildren()) {
      child.accept(this);
    }
  }

  /** Hook: por padrão, rodapé vazio. */
  protected void writeFooter(Document document) {}

  /** Visita a seção e desce nos filhos; subclasses só dizem como escrever o título. */
  @Override
  public void visitSection(Section section) {
    depth++;
    writeSectionTitle(section, depth);
    for (Node child : section.getChildren()) {
      child.accept(this);
    }
    depth--;
  }

  protected abstract void writeSectionTitle(Section section, int level);
}
