package br.pucpr.boletim;

import br.pucpr.docs.model.Document;
import br.pucpr.docs.model.Paragraph;
import br.pucpr.docs.model.Table;
import br.pucpr.docs.theme.ThemeFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * DOCX feito na mao: um .docx e so um zip com uns XMLs dentro. Nao usei o Apache POI pra nao
 * puxar dependencia grande so pra isso.
 */
public class DocxExporter extends AbstractBinaryExporter {
  private StringBuilder body;

  public DocxExporter(ThemeFactory theme, String aviso) {
    super(theme, aviso);
  }

  @Override
  public String contentType() {
    return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
  }

  @Override
  public String extensao() {
    return "docx";
  }

  @Override
  protected void comecar(Document d) {
    body = new StringBuilder();
    if (aviso != null) {
      par("[" + aviso + "]", true, 18);
    }
    par(theme.createTitleStyle().format(d.getTitle()), true, 36);
    par("Aluno: " + d.getAuthor(), false, 22);
  }

  @Override
  protected void escreverTitulo(String texto, int nivel) {
    par(texto, true, nivel == 1 ? 28 : 24);
  }

  @Override
  public void visitParagraph(Paragraph p) {
    par(p.getText(), false, 22);
  }

  @Override
  public void visitTable(Table t) {
    body.append("<w:tbl><w:tblPr><w:tblBorders>");
    for (String lado : new String[] {"top", "left", "bottom", "right", "insideH", "insideV"}) {
      body.append("<w:").append(lado).append(" w:val=\"single\" w:sz=\"4\"/>");
    }
    body.append("</w:tblBorders></w:tblPr>");
    linhaTabela(t.getHeaders(), true);
    for (var row : t.getRows()) {
      linhaTabela(row, false);
    }
    body.append("</w:tbl>");
    par("", false, 22);
  }

  @Override
  protected byte[] terminar(Document d) throws IOException {
    String documentXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        + "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
        + "<w:body>" + body + "</w:body></w:document>";
    String contentTypes = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
        + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
        + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
        + "<Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>"
        + "</Types>";
    String rels = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
        + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/>"
        + "</Relationships>";

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (ZipOutputStream zip = new ZipOutputStream(out)) {
      arquivo(zip, "[Content_Types].xml", contentTypes);
      arquivo(zip, "_rels/.rels", rels);
      arquivo(zip, "word/document.xml", documentXml);
    }
    return out.toByteArray();
  }

  private void linhaTabela(java.util.List<String> celulas, boolean negrito) {
    body.append("<w:tr>");
    for (String c : celulas) {
      body.append("<w:tc><w:tcPr><w:tcW w:w=\"2500\" w:type=\"dxa\"/></w:tcPr>");
      par(c, negrito, 22);
      body.append("</w:tc>");
    }
    body.append("</w:tr>");
  }

  private void par(String texto, boolean negrito, int tamanho) {
    body.append("<w:p><w:r><w:rPr>");
    if (negrito) {
      body.append("<w:b/>");
    }
    body.append("<w:sz w:val=\"").append(tamanho).append("\"/></w:rPr>");
    body.append("<w:t xml:space=\"preserve\">").append(esc(texto)).append("</w:t></w:r></w:p>");
  }

  private static void arquivo(ZipOutputStream zip, String nome, String conteudo) throws IOException {
    zip.putNextEntry(new ZipEntry(nome));
    zip.write(conteudo.getBytes(StandardCharsets.UTF_8));
    zip.closeEntry();
  }

  private static String esc(String s) {
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
  }
}
