package br.pucpr.docs.export;

import br.pucpr.docs.model.Document;
import br.pucpr.docs.model.Paragraph;
import br.pucpr.docs.model.Section;
import br.pucpr.docs.model.Table;
import br.pucpr.docs.theme.Palette;
import br.pucpr.docs.theme.ThemeFactory;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * PDF de verdade, escrito na mão (sem dependência). Segue o template method: o header abre a
 * primeira página com a faixa do título, os visit* desenham o conteúdo e o footer põe o rodapé
 * com "Página X de N" e monta o arquivo (objetos + xref).
 *
 * <p>Usa as cores e a fonte (Helvetica ou Times) da {@link Palette} do tema. Use
 * {@link #exportBytes(Document)} pra ter os bytes (o PDF é Latin-1). Os decorators de texto não
 * devem embrulhar este exportador, porque mexer na saída quebra o arquivo.
 */
public class PdfExporter extends AbstractExporter {
  private static final float LARGURA = 595;
  private static final float ALTURA = 842;
  private static final float MARGEM = 50;
  private static final float AREA = LARGURA - 2 * MARGEM;
  private static final float FUNDO = 70;

  private Palette p;
  private List<StringBuilder> paginas;
  private StringBuilder pg;
  private float y;
  private String tituloDoc;

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
    p = theme.createPalette();
    paginas = new ArrayList<>();
    tituloDoc = theme.createTitleStyle().format(d.getTitle());
    novaPagina(true);

    // faixa do título
    float alturaFaixa = 96;
    retangulo(0, ALTURA - alturaFaixa, LARGURA, alturaFaixa, p.headerBg(), null);
    retangulo(0, ALTURA - alturaFaixa - 4, LARGURA, 4, p.accent(), null);
    texto(MARGEM, ALTURA - 52, true, p.titleSize(), p.headerText(), tituloDoc);
    if (!d.getAuthor().isBlank()) {
      texto(MARGEM, ALTURA - 76, false, p.bodySize() + 1, p.headerText(), d.getAuthor());
    }
    y = ALTURA - alturaFaixa - 34;
  }

  @Override
  protected void writeSectionTitle(Section s, int level) {
    int tam = level == 1 ? p.headingSize() : p.headingSize() - 2;
    garantir(tam + 60);
    y -= 8;
    texto(MARGEM, y, true, tam, p.primary(), theme.createTitleStyle().format(s.getTitle()));
    y -= 6;
    linha(MARGEM, y, MARGEM + AREA, y, p.accent(), 1.2f);
    y -= tam + 4;
  }

  @Override
  public void visitParagraph(Paragraph par) {
    int tam = p.bodySize();
    for (String l : quebrar(par.getText(), tam, AREA)) {
      garantir(tam + 6);
      texto(MARGEM, y, false, tam, p.text(), l);
      y -= tam + 5;
    }
    y -= 6;
  }

  @Override
  public void visitTable(Table t) {
    int tam = p.bodySize();
    float alt = tam + 12;
    float[] col = larguras(t, tam);

    garantir(alt * 2);
    cabecalhoTabela(t, col, alt, tam);
    for (int r = 0; r < t.getRows().size(); r++) {
      List<String> row = t.getRows().get(r);
      if (y - alt < FUNDO) {
        novaPagina(false);
        cabecalhoTabela(t, col, alt, tam);
      }
      RowHighlighter.Highlight h = highlighter.highlight(t.getHeaders(), row);
      String fundo = r % 2 == 1 && p.zebra() != null ? p.zebra() : null;
      String cor = switch (h) {
        case POSITIVE -> p.positive();
        case NEGATIVE -> p.negative();
        default -> p.text();
      };
      celulas(row, col, alt, tam, h != RowHighlighter.Highlight.NONE, cor, fundo);
    }
    y -= 16;
  }

  @Override
  protected void writeFooter(Document d) {
    int total = paginas.size();
    for (int i = 0; i < total; i++) {
      pg = paginas.get(i);
      linha(MARGEM, 48, MARGEM + AREA, 48, p.border(), 0.5f);
      texto(MARGEM, 34, false, 9, p.muted(), tituloDoc);
      String num = "Página " + (i + 1) + " de " + total;
      texto(MARGEM + AREA - largura(num, 9), 34, false, 9, p.muted(), num);
    }
    montarArquivo();
  }

  // ---------- desenho ----------

  private void cabecalhoTabela(Table t, float[] col, float alt, int tam) {
    celulas(t.getHeaders(), col, alt, tam, true, p.tableHeaderText(), p.tableHeaderBg());
  }

  private void celulas(List<String> cells, float[] col, float alt, int tam, boolean negrito,
      String cor, String fundo) {
    float x = MARGEM;
    float base = y - alt;
    if (fundo != null) {
      retangulo(MARGEM, base, AREA, alt, fundo, null);
    }
    for (int i = 0; i < col.length; i++) {
      String c = i < cells.size() ? cells.get(i) : "";
      retangulo(x, base, col[i], alt, null, p.border());
      texto(x + 6, base + 6 + (alt - 12 - tam) / 2 + 1, negrito, tam, cor, cortar(c, tam, col[i] - 12));
      x += col[i];
    }
    y = base;
  }

  /** Largura de cada coluna proporcional ao maior texto dela. */
  private float[] larguras(Table t, int tam) {
    int n = t.getHeaders().size();
    float[] w = new float[n];
    float soma = 0;
    for (int i = 0; i < n; i++) {
      float max = largura(t.getHeaders().get(i), tam);
      for (List<String> row : t.getRows()) {
        if (i < row.size()) {
          max = Math.max(max, largura(row.get(i), tam));
        }
      }
      w[i] = Math.max(60, max + 16);
      soma += w[i];
    }
    for (int i = 0; i < n; i++) {
      w[i] = w[i] * AREA / soma;
    }
    return w;
  }

  private void garantir(float precisa) {
    if (y - precisa < FUNDO) {
      novaPagina(false);
    }
  }

  private void novaPagina(boolean primeira) {
    pg = new StringBuilder();
    paginas.add(pg);
    y = ALTURA - MARGEM;
    if (!primeira) {
      // cabeçalho pequeno nas outras páginas
      texto(MARGEM, ALTURA - 34, true, 9, p.primary(), tituloDoc);
      linha(MARGEM, ALTURA - 42, MARGEM + AREA, ALTURA - 42, p.accent(), 1f);
      y = ALTURA - 70;
    }
  }

  private void texto(float x, float yy, boolean negrito, int tam, String cor, String s) {
    pg.append(rgb(cor)).append(" rg\nBT /").append(negrito ? "F2 " : "F1 ").append(tam)
        .append(" Tf ").append(n(x)).append(' ').append(n(yy)).append(" Td (")
        .append(esc(s)).append(") Tj ET\n");
  }

  private void retangulo(float x, float yy, float w, float h, String fill, String stroke) {
    if (fill != null) {
      pg.append(rgb(fill)).append(" rg ").append(n(x)).append(' ').append(n(yy)).append(' ')
          .append(n(w)).append(' ').append(n(h)).append(" re f\n");
    }
    if (stroke != null) {
      pg.append("0.6 w ").append(rgb(stroke)).append(" RG ").append(n(x)).append(' ')
          .append(n(yy)).append(' ').append(n(w)).append(' ').append(n(h)).append(" re S\n");
    }
  }

  private void linha(float x1, float y1, float x2, float y2, String cor, float espessura) {
    pg.append(n(espessura)).append(" w ").append(rgb(cor)).append(" RG ").append(n(x1)).append(' ')
        .append(n(y1)).append(" m ").append(n(x2)).append(' ').append(n(y2)).append(" l S\n");
  }

  // ---------- texto ----------

  /** Largura aproximada do texto (média por caractere da fonte). */
  private float largura(String s, int tam) {
    float fator = p.pdfFont() == Palette.PdfFont.TIMES ? 0.47f : 0.53f;
    return s.length() * tam * fator;
  }

  private List<String> quebrar(String texto, int tam, float max) {
    List<String> linhas = new ArrayList<>();
    StringBuilder atual = new StringBuilder();
    for (String palavra : texto.split(" ")) {
      String teste = atual.length() == 0 ? palavra : atual + " " + palavra;
      if (largura(teste, tam) > max && atual.length() > 0) {
        linhas.add(atual.toString());
        atual = new StringBuilder(palavra);
      } else {
        atual = new StringBuilder(teste);
      }
    }
    linhas.add(atual.toString());
    return linhas;
  }

  private String cortar(String s, int tam, float max) {
    while (s.length() > 1 && largura(s, tam) > max) {
      s = s.substring(0, s.length() - 1);
    }
    return s;
  }

  // ---------- arquivo ----------

  private void montarArquivo() {
    List<String> objs = new ArrayList<>();
    objs.add("<< /Type /Catalog /Pages 2 0 R >>");
    StringBuilder kids = new StringBuilder();
    for (int i = 0; i < paginas.size(); i++) {
      kids.append(5 + i * 2).append(" 0 R ");
    }
    objs.add("<< /Type /Pages /Kids [" + kids + "] /Count " + paginas.size() + " >>");
    objs.add("<< /Type /Font /Subtype /Type1 /BaseFont /" + p.pdfFont().regular
        + " /Encoding /WinAnsiEncoding >>");
    objs.add("<< /Type /Font /Subtype /Type1 /BaseFont /" + p.pdfFont().bold
        + " /Encoding /WinAnsiEncoding >>");
    for (int i = 0; i < paginas.size(); i++) {
      objs.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
          + "/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents " + (6 + i * 2) + " 0 R >>");
      String c = paginas.get(i).toString();
      objs.add("<< /Length " + c.length() + " >>\nstream\n" + c + "\nendstream");
    }

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

  private static String rgb(String hex) {
    int c = Integer.parseInt(hex.substring(1), 16);
    return n(((c >> 16) & 255) / 255f) + " " + n(((c >> 8) & 255) / 255f) + " " + n((c & 255) / 255f);
  }

  private static String n(float v) {
    return String.format(Locale.US, "%.2f", v);
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
