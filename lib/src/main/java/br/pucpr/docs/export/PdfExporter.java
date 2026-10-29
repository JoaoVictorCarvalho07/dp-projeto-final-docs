package br.pucpr.docs.export;

import br.pucpr.docs.model.Document;
import br.pucpr.docs.model.Paragraph;
import br.pucpr.docs.model.Section;
import br.pucpr.docs.model.Table;
import br.pucpr.docs.theme.ThemeFactory;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * PDF de verdade, escrito na mão (sem dependência). Segue o mesmo template method: o header, os
 * visit* e o footer só juntam linhas; no writeFooter as linhas viram páginas e objetos PDF.
 *
 * <p>Use {@link #exportBytes(Document)} pra ter os bytes certos (o PDF é Latin-1). Os decorators de
 * texto não devem embrulhar este exportador, porque mexer na saída quebra o arquivo.
 */
public class PdfExporter extends AbstractExporter {
  private static final int LINHAS_POR_PAGINA = 45;

  /** Uma linha do PDF: texto, tamanho da fonte e se é negrito. */
  private record Linha(String texto, int tamanho, boolean negrito) {}

  private List<Linha> linhas;

  public PdfExporter() {}

  public PdfExporter(ThemeFactory theme) {
    super(theme);
  }

  /** Exporta e já devolve os bytes do arquivo .pdf. */
  public byte[] exportBytes(Document d) {
    return export(d).getBytes(StandardCharsets.ISO_8859_1);
  }

  @Override
  protected void writeHeader(Document d) {
    linhas = new ArrayList<>();
    linhas.add(new Linha(theme.createTitleStyle().format(d.getTitle()), 18, true));
    if (!d.getAuthor().isBlank()) {
      linhas.add(new Linha("Autor: " + d.getAuthor(), 11, false));
    }
    linhas.add(new Linha("", 11, false));
  }

  @Override
  protected void writeSectionTitle(Section s, int level) {
    linhas.add(new Linha(theme.createTitleStyle().format(s.getTitle()), level == 1 ? 14 : 12, true));
  }

  @Override
  public void visitParagraph(Paragraph p) {
    linhas.add(new Linha(p.getText(), 11, false));
  }

  @Override
  public void visitTable(Table t) {
    String sep = theme.createTableStyle().columnSeparator();
    linhas.add(new Linha(String.join(sep, t.getHeaders()), 11, true));
    for (var row : t.getRows()) {
      linhas.add(new Linha(String.join(sep, row), 11, false));
    }
    linhas.add(new Linha("", 11, false));
  }

  @Override
  protected void writeFooter(Document d) {
    // quebra em páginas
    List<String> conteudos = new ArrayList<>();
    for (int i = 0; i < linhas.size(); i += LINHAS_POR_PAGINA) {
      List<Linha> pagina = linhas.subList(i, Math.min(i + LINHAS_POR_PAGINA, linhas.size()));
      conteudos.add(conteudoDaPagina(pagina));
    }

    // objetos: 1 catalog, 2 pages, 3 fonte normal, 4 fonte negrito, depois (page, content) por página
    List<String> objs = new ArrayList<>();
    objs.add("<< /Type /Catalog /Pages 2 0 R >>");
    StringBuilder kids = new StringBuilder();
    for (int i = 0; i < conteudos.size(); i++) {
      kids.append(5 + i * 2).append(" 0 R ");
    }
    objs.add("<< /Type /Pages /Kids [" + kids + "] /Count " + conteudos.size() + " >>");
    objs.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>");
    objs.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>");
    for (int i = 0; i < conteudos.size(); i++) {
      int contentId = 6 + i * 2;
      objs.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
          + "/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents " + contentId + " 0 R >>");
      String c = conteudos.get(i);
      objs.add("<< /Length " + c.length() + " >>\nstream\n" + c + "\nendstream");
    }

    // monta o arquivo com a tabela xref (offset de cada objeto)
    StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
    List<Integer> offsets = new ArrayList<>();
    for (int i = 0; i < objs.size(); i++) {
      offsets.add(pdf.length());
      pdf.append(i + 1).append(" 0 obj\n").append(objs.get(i)).append("\nendobj\n");
    }
    int xref = pdf.length();
    pdf.append("xref\n0 ").append(objs.size() + 1).append("\n0000000000 65535 f \n");
    for (int off : offsets) {
      pdf.append(String.format("%010d 00000 n \n", off));
    }
    pdf.append("trailer\n<< /Size ").append(objs.size() + 1).append(" /Root 1 0 R >>\n");
    pdf.append("startxref\n").append(xref).append("\n%%EOF\n");

    out.setLength(0);
    out.append(pdf);
  }

  private String conteudoDaPagina(List<Linha> pagina) {
    StringBuilder c = new StringBuilder("BT\n50 792 Td\n");
    for (Linha l : pagina) {
      c.append(l.negrito() ? "/F2 " : "/F1 ").append(l.tamanho()).append(" Tf\n");
      c.append("(").append(esc(l.texto())).append(") Tj\n");
      c.append("0 -").append(l.tamanho() + 5).append(" Td\n");
    }
    return c.append("ET").toString();
  }

  /** Escapa parênteses e barra; troca o que não existe em Latin-1 por '?'. */
  private static String esc(String s) {
    StringBuilder sb = new StringBuilder();
    for (char ch : s.toCharArray()) {
      if (ch == '(' || ch == ')' || ch == '\\') {
        sb.append('\\').append(ch);
      } else if (ch > 255) {
        sb.append('?');
      } else {
        sb.append(ch);
      }
    }
    return sb.toString();
  }
}
