package br.pucpr.docs.decorator;

import br.pucpr.docs.export.DocumentExporter;

/** DECORATOR: quebra a saída em páginas de N linhas e numera cada uma. */
public final class PageNumberDecorator extends ExporterDecorator {
  private final int linesPerPage;

  public PageNumberDecorator(DocumentExporter inner, int linesPerPage) {
    super(inner);
    if (linesPerPage <= 0) {
      throw new IllegalArgumentException("linesPerPage deve ser > 0");
    }
    this.linesPerPage = linesPerPage;
  }

  @Override
  protected String decorate(String output) {
    String[] lines = output.split("\n", -1);
    int count = lines.length;
    if (count > 0 && lines[count - 1].isEmpty()) {
      count--;
    }
    int pages = Math.max(1, (count + linesPerPage - 1) / linesPerPage);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < count; i++) {
      sb.append(lines[i]).append("\n");
      if ((i + 1) % linesPerPage == 0 || i == count - 1) {
        int page = i / linesPerPage + 1;
        sb.append("--- Página ").append(page).append('/').append(pages).append(" ---\n");
      }
    }
    return sb.toString();
  }
}
