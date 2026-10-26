package br.pucpr.docs.export;

import br.pucpr.docs.model.Document;
import br.pucpr.docs.model.Paragraph;
import br.pucpr.docs.model.Section;
import br.pucpr.docs.model.Table;
import br.pucpr.docs.theme.ThemeFactory;

/**
 * "PDF" simplificado: gera texto paginável que imita um PDF. Pra PDF real, troque o corpo por uma
 * lib como OpenPDF/PDFBox. O fluxo (template method) continua igual.
 */
public class PdfTextExporter extends AbstractExporter {
  public PdfTextExporter() {}

  public PdfTextExporter(ThemeFactory theme) {
    super(theme);
  }

  @Override
  protected void writeHeader(Document d) {
    out.append("%PDF-simplificado\n");
    out.append("==== ").append(theme.createTitleStyle().format(d.getTitle())).append(" ====\n\n");
  }

  @Override
  protected void writeFooter(Document d) {
    out.append("\n---- fim do documento (").append(d.getAuthor()).append(") ----\n");
  }

  @Override
  protected void writeSectionTitle(Section s, int level) {
    out.append("  ".repeat(level - 1)).append(theme.createTitleStyle().format(s.getTitle()));
    out.append("\n");
  }

  @Override
  public void visitParagraph(Paragraph p) {
    out.append("  ".repeat(depth)).append(p.getText()).append("\n");
  }

  @Override
  public void visitTable(Table t) {
    String sep = theme.createTableStyle().columnSeparator();
    out.append("  ".repeat(depth)).append(String.join(sep, t.getHeaders())).append("\n");
    for (var row : t.getRows()) {
      out.append("  ".repeat(depth)).append(String.join(sep, row)).append("\n");
    }
  }
}
