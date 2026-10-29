package br.pucpr.boletim;

import br.pucpr.docs.decorator.ExporterDecorator;
import br.pucpr.docs.export.DocumentExporter;

/** Decorator novo: coloca um aviso de CONFIDENCIAL no topo da saida. */
public class ConfidencialDecorator extends ExporterDecorator {

  public ConfidencialDecorator(DocumentExporter inner) {
    super(inner);
  }

  @Override
  protected String decorate(String output) {
    return "*** CONFIDENCIAL - uso exclusivo do aluno ***\n" + output;
  }
}
