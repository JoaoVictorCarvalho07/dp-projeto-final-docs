package br.pucpr.docs.decorator;

import br.pucpr.docs.export.DocumentExporter;

/** DECORATOR: injeta uma marca d'água visual no HTML, antes do </body>. */
public final class HtmlWatermarkDecorator extends ExporterDecorator {
  private final String text;

  public HtmlWatermarkDecorator(DocumentExporter inner, String text) {
    super(inner);
    this.text = text;
  }

  @Override
  protected String decorate(String html) {
    String marca = "<style>\n"
        + ".marca-dagua{position:fixed;top:50%;left:50%;"
        + "transform:translate(-50%,-50%) rotate(-30deg);"
        + "opacity:.12;font-size:6rem;font-weight:bold;"
        + "pointer-events:none;user-select:none;z-index:9999;white-space:nowrap}\n"
        + "</style>\n"
        + "<div class=\"marca-dagua\">" + esc(text) + "</div>\n";
    int fim = html.indexOf("</body>");
    if (fim < 0) {
      return html + marca;
    }
    return html.substring(0, fim) + marca + html.substring(fim);
  }

  private static String esc(String s) {
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
