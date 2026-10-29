package br.pucpr.boletim;

import br.pucpr.docs.export.AbstractExporter;
import br.pucpr.docs.model.Document;
import br.pucpr.docs.model.Paragraph;
import br.pucpr.docs.model.Section;
import br.pucpr.docs.model.Table;
import br.pucpr.docs.theme.ThemeFactory;

/**
 * Exportador novo feito aqui no boletim, fora da lib. So estende o AbstractExporter e preenche os
 * passos do template (cabecalho, titulo de secao, paragrafo, tabela).
 */
public class LatexExporter extends AbstractExporter {

  public LatexExporter() {}

  public LatexExporter(ThemeFactory theme) {
    super(theme);
  }

  @Override
  protected void writeHeader(Document d) {
    out.append("\\documentclass{article}\n");
    out.append("\\title{").append(esc(theme.createTitleStyle().format(d.getTitle()))).append("}\n");
    out.append("\\author{").append(esc(d.getAuthor())).append("}\n");
    out.append("\\begin{document}\n\\maketitle\n");
  }

  @Override
  protected void writeFooter(Document d) {
    out.append("\\end{document}\n");
  }

  @Override
  protected void writeSectionTitle(Section s, int level) {
    String cmd = level == 1 ? "section" : level == 2 ? "subsection" : "subsubsection";
    out.append("\\").append(cmd).append("{")
        .append(esc(theme.createTitleStyle().format(s.getTitle()))).append("}\n");
  }

  @Override
  public void visitParagraph(Paragraph p) {
    out.append(esc(p.getText())).append("\n\n");
  }

  @Override
  public void visitTable(Table t) {
    out.append("\\begin{tabular}{|").append("l|".repeat(t.getHeaders().size())).append("}\n\\hline\n");
    out.append(linha(t.getHeaders())).append(" \\\\\n\\hline\n");
    for (var row : t.getRows()) {
      out.append(linha(row)).append(" \\\\\n");
    }
    out.append("\\hline\n\\end{tabular}\n\n");
  }

  private static String linha(java.util.List<String> cells) {
    return String.join(" & ", cells.stream().map(LatexExporter::esc).toList());
  }

  private static String esc(String s) {
    return s.replace("\\", "\\textbackslash{}").replace("&", "\\&").replace("%", "\\%")
        .replace("_", "\\_").replace("#", "\\#").replace("$", "\\$");
  }
}
