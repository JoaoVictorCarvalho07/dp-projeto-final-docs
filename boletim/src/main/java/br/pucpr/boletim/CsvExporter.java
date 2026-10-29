package br.pucpr.boletim;

import br.pucpr.docs.export.AbstractExporter;
import br.pucpr.docs.model.Document;
import br.pucpr.docs.model.Paragraph;
import br.pucpr.docs.model.Section;
import br.pucpr.docs.model.Table;
import java.util.List;
import java.util.Locale;

/**
 * Exportador CSV feito aqui no boletim, fora da lib (Open/Closed). Usa ';' como separador e
 * comeca com BOM pro Excel em portugues abrir certinho com acento.
 * Colunas: Materia;Professor;Avaliacao;Nota
 */
public class CsvExporter extends AbstractExporter {
  private static final String BOM = "\uFEFF";

  private String materia;
  private String professor;
  private double somaMedias;
  private int qtdMedias;

  @Override
  protected void writeHeader(Document d) {
    materia = "";
    professor = "";
    somaMedias = 0;
    qtdMedias = 0;
    out.append(BOM).append("Materia;Professor;Avaliacao;Nota\n");
  }

  @Override
  protected void writeFooter(Document d) {
    if (qtdMedias > 0) {
      out.append("Resumo;;Media geral;")
          .append(String.format(Locale.US, "%.1f", somaMedias / qtdMedias)).append("\n");
    }
  }

  @Override
  protected void writeSectionTitle(Section s, int level) {
    // o titulo cru, sem o format do tema, pra nao sujar a coluna
    materia = s.getTitle();
    professor = "";
  }

  @Override
  public void visitParagraph(Paragraph p) {
    // o paragrafo da materia e "Professor(a): Fulano - Situacao: ..."
    String t = p.getText();
    if (t.startsWith("Professor(a): ")) {
      int fim = t.indexOf(" - ");
      professor = fim > 0 ? t.substring(14, fim) : t.substring(14);
    }
  }

  @Override
  public void visitTable(Table t) {
    for (List<String> row : t.getRows()) {
      String avaliacao = row.get(0);
      String nota = row.size() > 1 ? row.get(1) : "";
      out.append(cel(materia)).append(';').append(cel(professor)).append(';')
          .append(cel(avaliacao)).append(';').append(cel(nota)).append("\n");
      if (avaliacao.equals("Media")) {
        somaMedias += Double.parseDouble(nota);
        qtdMedias++;
      }
    }
  }

  /** Se tiver ; ou aspas, coloca entre aspas (regra normal de CSV). */
  private static String cel(String s) {
    if (s.contains(";") || s.contains("\"") || s.contains("\n")) {
      return "\"" + s.replace("\"", "\"\"") + "\"";
    }
    return s;
  }
}
