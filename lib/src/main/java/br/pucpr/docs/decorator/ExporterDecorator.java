package br.pucpr.docs.decorator;

import br.pucpr.docs.export.DocumentExporter;
import br.pucpr.docs.model.Document;

/**
 * DECORATOR: embrulha um exportador e acrescenta comportamento na saída. Dá pra empilhar quantos
 * quiser: new PageNumber(new Watermark(new HtmlExporter(), "X")).
 */
public abstract class ExporterDecorator implements DocumentExporter {
  protected final DocumentExporter inner;

  protected ExporterDecorator(DocumentExporter inner) {
    this.inner = inner;
  }

  @Override
  public String export(Document document) {
    return decorate(inner.export(document));
  }

  protected abstract String decorate(String output);
}
