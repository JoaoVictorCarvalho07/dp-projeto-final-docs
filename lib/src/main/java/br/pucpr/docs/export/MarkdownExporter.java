package br.pucpr.docs.export;

import br.pucpr.docs.model.Document;
import br.pucpr.docs.model.Paragraph;
import br.pucpr.docs.model.Section;
import br.pucpr.docs.model.Table;
import br.pucpr.docs.theme.ThemeFactory;

/** Exporta para Markdown. */
public class MarkdownExporter extends AbstractExporter {
  public MarkdownExporter() {}

  public MarkdownExporter(ThemeFactory theme) {
    super(theme);
  }

  @Override
  protected void writeHeader(Document d) {
    out.append("# ").append(theme.createTitleStyle().format(d.getTitle())).append("\n\n");
    if (!d.getAuthor().isBlank()) {
      out.append("_Autor: ").append(d.getAuthor()).append("_\n\n");
    }
  }

  @Override
  protected void writeSectionTitle(Section s, int level) {
    out.append("#".repeat(Math.min(level + 1, 6))).append(' ');
    out.append(theme.createTitleStyle().format(s.getTitle())).append("\n\n");
  }

  @Override
  public void visitParagraph(Paragraph p) {
    out.append(p.getText()).append("\n\n");
  }

  @Override
  public void visitTable(Table t) {
    out.append("| ").append(String.join(" | ", t.getHeaders())).append(" |\n");
    out.append("|").append(" --- |".repeat(t.getHeaders().size())).append("\n");
    for (var row : t.getRows()) {
      boolean forte = highlighter.highlight(t.getHeaders(), row) != RowHighlighter.Highlight.NONE;
      out.append("| ");
      for (int i = 0; i < row.size(); i++) {
        if (i > 0) {
          out.append(" | ");
        }
        out.append(forte ? "**" + row.get(i) + "**" : row.get(i));
      }
      out.append(" |\n");
    }
    out.append("\n");
  }
}
