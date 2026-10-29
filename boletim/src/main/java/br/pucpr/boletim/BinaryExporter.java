package br.pucpr.boletim;

import br.pucpr.docs.model.Document;

/**
 * A interface da lib (DocumentExporter) devolve String, e PDF/DOCX sao binarios.
 * Em vez de mudar a lib, criamos essa interface aqui no boletim.
 */
public interface BinaryExporter {
  byte[] export(Document document);

  String contentType();

  String extensao();
}
