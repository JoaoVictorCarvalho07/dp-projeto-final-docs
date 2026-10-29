package br.pucpr.boletim;

import br.pucpr.docs.export.RowHighlighter;
import br.pucpr.docs.model.Document;
import br.pucpr.docs.model.Node;
import br.pucpr.docs.model.Section;
import br.pucpr.docs.theme.ThemeFactory;
import br.pucpr.docs.visitor.DocumentVisitor;

/**
 * Base dos exportadores binarios. Mesma ideia do AbstractExporter da lib (template method),
 * so que o resultado e byte[]. Usa o DocumentVisitor da lib pra andar na arvore.
 */
public abstract class AbstractBinaryExporter implements BinaryExporter, DocumentVisitor {
  protected final ThemeFactory theme;
  protected final String aviso;
  protected int depth;
  protected RowHighlighter highlighter = RowHighlighter.NONE;

  /** aviso: texto que vai no topo (ex: "RASCUNHO"), pode ser null. */
  protected AbstractBinaryExporter(ThemeFactory theme, String aviso) {
    this.theme = theme;
    this.aviso = aviso;
  }

  /** Mesmo destaque de linhas que os exportadores da lib usam. */
  public AbstractBinaryExporter highlightRows(RowHighlighter h) {
    this.highlighter = h == null ? RowHighlighter.NONE : h;
    return this;
  }

  @Override
  public final byte[] export(Document document) {
    depth = 0;
    try {
      comecar(document);
      for (Node child : document.getRoot().getChildren()) {
        child.accept(this);
      }
      return terminar(document);
    } catch (Exception e) {
      throw new RuntimeException("erro exportando " + extensao(), e);
    }
  }

  protected abstract void comecar(Document document) throws Exception;

  protected abstract byte[] terminar(Document document) throws Exception;

  protected abstract void escreverTitulo(String texto, int nivel);

  @Override
  public void visitSection(Section section) {
    depth++;
    escreverTitulo(theme.createTitleStyle().format(section.getTitle()), depth);
    for (Node child : section.getChildren()) {
      child.accept(this);
    }
    depth--;
  }
}
