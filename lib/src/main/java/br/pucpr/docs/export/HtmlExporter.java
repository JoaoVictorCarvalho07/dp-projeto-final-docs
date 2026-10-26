package br.pucpr.docs.export;

import br.pucpr.docs.model.Document;
import br.pucpr.docs.model.Paragraph;
import br.pucpr.docs.model.Section;
import br.pucpr.docs.model.Table;
import br.pucpr.docs.theme.ThemeFactory;

/** Exporta para HTML usando o CSS do tema. */
public class HtmlExporter extends AbstractExporter {
  public HtmlExporter() {}

  public HtmlExporter(ThemeFactory theme) {
    super(theme);
  }

  @Override
  protected void writeHeader(Document d) {
    out.append("<html><head><title>").append(esc(d.getTitle())).append("</title></head><body>\n");
    out.append("<h1 style=\"")
        .append(theme.createTitleStyle().css())
        .append("\">")
        .append(esc(d.getTitle()))
        .append("</h1>\n");
  }

  @Override
  protected void writeFooter(Document d) {
    out.append("<footer>").append(esc(d.getAuthor())).append("</footer>\n</body></html>\n");
  }

  @Override
  protected void writeSectionTitle(Section s, int level) {
    int h = Math.min(level + 1, 6);
    out.append("<h").append(h).append(" style=\"").append(theme.createTitleStyle().css());
    out.append("\">").append(esc(s.getTitle())).append("</h").append(h).append(">\n");
  }

  @Override
  public void visitParagraph(Paragraph p) {
    out.append("<p style=\"").append(theme.createParagraphStyle().css()).append("\">");
    out.append(esc(p.getText())).append("</p>\n");
  }

  @Override
  public void visitTable(Table t) {
    out.append("<table style=\"").append(theme.createTableStyle().css()).append("\">\n<tr>");
    t.getHeaders().forEach(h -> out.append("<th>").append(esc(h)).append("</th>"));
    out.append("</tr>\n");
    for (var row : t.getRows()) {
      out.append("<tr>");
      row.forEach(c -> out.append("<td>").append(esc(c)).append("</td>"));
      out.append("</tr>\n");
    }
    out.append("</table>\n");
  }

  private static String esc(String s) {
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
