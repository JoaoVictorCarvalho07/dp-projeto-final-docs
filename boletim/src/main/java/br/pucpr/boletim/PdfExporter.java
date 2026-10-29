package br.pucpr.boletim;

import br.pucpr.docs.model.Document;
import br.pucpr.docs.model.Paragraph;
import br.pucpr.docs.model.Table;
import br.pucpr.docs.theme.ThemeFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/** Gera um PDF de verdade com o PDFBox. */
public class PdfExporter extends AbstractBinaryExporter {
  private static final float MARGEM = 50;
  private final PDType1Font normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
  private final PDType1Font negrito = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

  private PDDocument pdf;
  private PDPageContentStream cs;
  private float y;
  private int paginas;

  public PdfExporter(ThemeFactory theme, String aviso) {
    super(theme, aviso);
  }

  @Override
  public String contentType() {
    return "application/pdf";
  }

  @Override
  public String extensao() {
    return "pdf";
  }

  @Override
  protected void comecar(Document d) throws IOException {
    pdf = new PDDocument();
    paginas = 0;
    novaPagina();
    linha(negrito, 18, theme.createTitleStyle().format(d.getTitle()));
    linha(normal, 11, "Aluno: " + d.getAuthor());
    y -= 10;
  }

  @Override
  protected byte[] terminar(Document d) throws IOException {
    cs.close();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    pdf.save(out);
    pdf.close();
    return out.toByteArray();
  }

  @Override
  protected void escreverTitulo(String texto, int nivel) {
    y -= 8;
    linha(negrito, nivel == 1 ? 14 : 12, texto);
  }

  @Override
  public void visitParagraph(Paragraph p) {
    linha(normal, 11, p.getText());
  }

  @Override
  public void visitTable(Table t) {
    String sep = theme.createTableStyle().columnSeparator();
    linha(negrito, 11, String.join(sep, t.getHeaders()));
    for (var row : t.getRows()) {
      linha(normal, 11, String.join(sep, row));
    }
    y -= 6;
  }

  private void novaPagina() throws IOException {
    if (cs != null) {
      cs.close();
    }
    PDPage page = new PDPage(PDRectangle.A4);
    pdf.addPage(page);
    paginas++;
    cs = new PDPageContentStream(pdf, page);
    y = page.getMediaBox().getHeight() - MARGEM;
    if (aviso != null) {
      linha(negrito, 9, "[" + aviso + "]");
    }
    // numero da pagina no rodape
    cs.beginText();
    cs.setFont(normal, 9);
    cs.newLineAtOffset(MARGEM, 30);
    cs.showText("Pagina " + paginas);
    cs.endText();
  }

  private void linha(PDType1Font fonte, float tamanho, String texto) {
    try {
      if (y < MARGEM + 20) {
        novaPagina();
      }
      cs.beginText();
      cs.setFont(fonte, tamanho);
      cs.newLineAtOffset(MARGEM, y);
      cs.showText(texto);
      cs.endText();
      y -= tamanho + 6;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
