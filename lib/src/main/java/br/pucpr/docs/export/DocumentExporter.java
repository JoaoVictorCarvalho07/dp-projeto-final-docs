package br.pucpr.docs.export;

import br.pucpr.docs.model.Document;

/** Interface comum de qualquer exportador (base para o DECORATOR). */
public interface DocumentExporter {
  String export(Document document);
}
