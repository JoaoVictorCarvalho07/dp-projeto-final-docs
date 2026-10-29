package br.pucpr.docs.export;

import br.pucpr.docs.model.Document;
import br.pucpr.docs.model.Paragraph;
import br.pucpr.docs.model.Section;
import br.pucpr.docs.model.Table;
import br.pucpr.docs.theme.Palette;
import br.pucpr.docs.theme.ThemeFactory;
import java.util.List;

/** Exporta para uma página HTML completa, com o CSS do tema embutido. */
public class HtmlExporter extends AbstractExporter {
  public HtmlExporter() {}

  public HtmlExporter(ThemeFactory theme) {
    super(theme);
  }

  @Override
  protected void writeHeader(Document d) {
    Palette p = theme.createPalette();
    out.append("<!DOCTYPE html>\n<html lang=\"pt-BR\"><head><meta charset=\"utf-8\">\n");
    out.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
    out.append("<title>").append(esc(d.getTitle())).append("</title>\n<style>\n");
    out.append(css(p));
    out.append("</style></head><body>\n");
    out.append("<header class=\"doc-header\"><div class=\"wrap\"><h1>")
        .append(esc(theme.createTitleStyle().format(d.getTitle()))).append("</h1>");
    if (!d.getAuthor().isBlank()) {
      out.append("<p class=\"autor\">").append(esc(d.getAuthor())).append("</p>");
    }
    out.append("</div></header>\n<main class=\"wrap\">\n");
  }

  @Override
  protected void writeFooter(Document d) {
    out.append("</main>\n<footer class=\"doc-footer\"><div class=\"wrap\">")
        .append(esc(d.getTitle()));
    if (!d.getAuthor().isBlank()) {
      out.append(" &middot; ").append(esc(d.getAuthor()));
    }
    out.append("</div></footer>\n</body></html>\n");
  }

  @Override
  public void visitSection(Section s) {
    out.append("<section>\n");
    super.visitSection(s);
    out.append("</section>\n");
  }

  @Override
  protected void writeSectionTitle(Section s, int level) {
    int h = Math.min(level + 1, 6);
    out.append("<h").append(h).append(">").append(esc(theme.createTitleStyle().format(s.getTitle())));
    out.append("</h").append(h).append(">\n");
  }

  @Override
  public void visitParagraph(Paragraph p) {
    out.append("<p>").append(esc(p.getText())).append("</p>\n");
  }

  @Override
  public void visitTable(Table t) {
    out.append("<table>\n<thead><tr>");
    t.getHeaders().forEach(h -> out.append("<th>").append(esc(h)).append("</th>"));
    out.append("</tr></thead>\n<tbody>\n");
    for (List<String> row : t.getRows()) {
      String cls = switch (highlighter.highlight(t.getHeaders(), row)) {
        case STRONG -> " class=\"forte\"";
        case POSITIVE -> " class=\"forte positivo\"";
        case NEGATIVE -> " class=\"forte negativo\"";
        default -> "";
      };
      out.append("<tr").append(cls).append(">");
      row.forEach(c -> out.append("<td>").append(esc(c)).append("</td>"));
      out.append("</tr>\n");
    }
    out.append("</tbody></table>\n");
  }

  /** CSS montado a partir da paleta do tema. */
  private String css(Palette p) {
    String zebra = p.zebra() == null ? "" : "tbody tr:nth-child(even){background:" + p.zebra() + "}\n";
    return "*{box-sizing:border-box}\n"
        + "body{margin:0;font-family:" + p.cssFont() + ";color:" + p.text() + ";background:"
        + p.background() + ";font-size:" + (p.bodySize() + 4) + "px;line-height:1.55}\n"
        + ".wrap{max-width:820px;margin:0 auto;padding:0 24px}\n"
        + ".doc-header{background:" + p.headerBg() + ";color:" + p.headerText() + ";padding:32px 0 24px;"
        + "border-bottom:4px solid " + p.accent() + "}\n"
        + ".doc-header h1{margin:0;font-size:" + (p.titleSize() + 8) + "px;letter-spacing:.5px}\n"
        + ".doc-header h1{" + theme.createTitleStyle().css() + "color:" + p.headerText() + "}\n"
        + ".autor{margin:6px 0 0;opacity:.85}\n"
        + "main{padding:12px 24px 40px}\n"
        + "section{margin-top:28px}\n"
        + "h2,h3,h4{color:" + p.primary() + ";margin:0 0 8px;font-size:" + (p.headingSize() + 6) + "px;"
        + "border-bottom:2px solid " + p.accent() + ";padding-bottom:4px}\n"
        + "p{margin:0 0 12px;" + theme.createParagraphStyle().css() + "}\n"
        + "table{" + theme.createTableStyle().css() + "border-collapse:collapse;width:100%;margin:8px 0 4px;"
        + "border:1px solid " + p.border() + "}\n"
        + "th{background:" + p.tableHeaderBg() + ";color:" + p.tableHeaderText() + ";text-align:left}\n"
        + "th,td{padding:8px 12px;border:1px solid " + p.border() + "}\n"
        + zebra
        + "tr.forte td{font-weight:bold}\n"
        + "tr.positivo td{color:" + p.positive() + "}\n"
        + "tr.negativo td{color:" + p.negative() + "}\n"
        + ".doc-footer{border-top:1px solid " + p.border() + ";color:" + p.muted()
        + ";font-size:13px;padding:16px 0}\n";
  }

  private static String esc(String s) {
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
