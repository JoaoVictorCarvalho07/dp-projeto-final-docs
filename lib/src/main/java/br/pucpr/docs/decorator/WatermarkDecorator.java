package br.pucpr.docs.decorator;

import br.pucpr.docs.export.DocumentExporter;

/** DECORATOR: adiciona uma marca d'água no topo e no fim da saída. */
public final class WatermarkDecorator extends ExporterDecorator {
  private final String text;

  public WatermarkDecorator(DocumentExporter inner, String text) {
    super(inner);
    this.text = text;
  }

  @Override
  protected String decorate(String output) {
    String mark = "[MARCA D'ÁGUA: " + text + "]\n";
    return mark + output + mark;
  }
}
