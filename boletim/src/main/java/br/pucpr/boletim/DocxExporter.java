package br.pucpr.boletim;

import br.pucpr.docs.export.RowHighlighter;
import br.pucpr.docs.model.Document;
import br.pucpr.docs.model.Paragraph;
import br.pucpr.docs.model.Table;
import br.pucpr.docs.theme.Palette;
import br.pucpr.docs.theme.ThemeFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * DOCX feito na mao: um .docx e so um zip com uns XMLs dentro. Nao usei o Apache POI pra nao
 * puxar dependencia grande so pra isso. Tem estilos de titulo (styles.xml), tabela com borda e
 * cabecalho sombreado e rodape com numero de pagina, tudo com as cores e a fonte do tema.
 */
public class DocxExporter extends AbstractBinaryExporter {
  private static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
  private static final String R = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";

  private StringBuilder body;
  private Palette p;
  private String tituloDoc;

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
    p = theme.createPalette();
    body = new StringBuilder();
    tituloDoc = theme.createTitleStyle().format(d.getTitle());
    if (aviso != null) {
      body.append("<w:p><w:pPr><w:jc w:val=\"right\"/></w:pPr><w:r><w:rPr><w:b/><w:color w:val=\"")
          .append(hex(p.muted())).append("\"/><w:sz w:val=\"16\"/></w:rPr><w:t>")
          .append(esc(aviso)).append("</w:t></w:r></w:p>");
    }
    body.append("<w:p><w:pPr><w:pStyle w:val=\"Title\"/></w:pPr><w:r><w:t xml:space=\"preserve\"> ")
        .append(esc(tituloDoc)).append("</w:t></w:r></w:p>");
    if (!d.getAuthor().isBlank()) {
      body.append("<w:p><w:pPr><w:pStyle w:val=\"Subtitle\"/></w:pPr><w:r><w:t>")
          .append(esc(d.getAuthor())).append("</w:t></w:r></w:p>");
    }
  }

  @Override
  protected void escreverTitulo(String texto, int nivel) {
    body.append("<w:p><w:pPr><w:pStyle w:val=\"Heading").append(Math.min(nivel, 2))
        .append("\"/></w:pPr><w:r><w:t>").append(esc(texto)).append("</w:t></w:r></w:p>");
  }

  @Override
  public void visitParagraph(Paragraph par) {
    body.append("<w:p><w:r><w:t xml:space=\"preserve\">").append(esc(par.getText()))
        .append("</w:t></w:r></w:p>");
  }

  @Override
  public void visitTable(Table t) {
    String borda = hex(p.border());
    body.append("<w:tbl><w:tblPr><w:tblW w:w=\"5000\" w:type=\"pct\"/><w:tblBorders>");
    for (String lado : new String[] {"top", "left", "bottom", "right", "insideH", "insideV"}) {
      body.append("<w:").append(lado).append(" w:val=\"single\" w:sz=\"6\" w:color=\"")
          .append(borda).append("\"/>");
    }
    body.append("</w:tblBorders><w:tblCellMar><w:left w:w=\"120\" w:type=\"dxa\"/>")
        .append("<w:right w:w=\"120\" w:type=\"dxa\"/></w:tblCellMar></w:tblPr>");

    linha(t.getHeaders(), true, hex(p.tableHeaderText()), hex(p.tableHeaderBg()), true);
    for (int i = 0; i < t.getRows().size(); i++) {
      List<String> row = t.getRows().get(i);
      RowHighlighter.Highlight h = highlighter.highlight(t.getHeaders(), row);
      String cor = switch (h) {
        case POSITIVE -> hex(p.positive());
        case NEGATIVE -> hex(p.negative());
        default -> hex(p.text());
      };
      String fundo = i % 2 == 1 && p.zebra() != null ? hex(p.zebra()) : null;
      linha(row, h != RowHighlighter.Highlight.NONE, cor, fundo, false);
    }
    body.append("</w:tbl><w:p/>");
  }

  private void linha(List<String> cells, boolean negrito, String cor, String fundo, boolean cabecalho) {
    body.append("<w:tr>");
    if (cabecalho) {
      body.append("<w:trPr><w:tblHeader/></w:trPr>");
    }
    for (String c : cells) {
      body.append("<w:tc><w:tcPr>");
      if (fundo != null) {
        body.append("<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"").append(fundo).append("\"/>");
      }
      body.append("</w:tcPr><w:p><w:pPr><w:spacing w:before=\"60\" w:after=\"60\"/></w:pPr><w:r><w:rPr>");
      if (negrito) {
        body.append("<w:b/>");
      }
      body.append("<w:color w:val=\"").append(cor).append("\"/></w:rPr><w:t xml:space=\"preserve\">")
          .append(esc(c)).append("</w:t></w:r></w:p></w:tc>");
    }
    body.append("</w:tr>");
  }

  @Override
  protected byte[] terminar(Document d) throws IOException {
    String documentXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        + "<w:document xmlns:w=\"" + W + "\" xmlns:r=\"" + R + "\"><w:body>" + body
        + "<w:sectPr><w:footerReference w:type=\"default\" r:id=\"rId2\"/>"
        + "<w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
        + "<w:pgMar w:top=\"1134\" w:right=\"1134\" w:bottom=\"1134\" w:left=\"1134\" w:header=\"567\" w:footer=\"567\"/>"
        + "</w:sectPr></w:body></w:document>";

    String footer = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
        + "<w:ftr xmlns:w=\"" + W + "\"><w:p><w:pPr><w:pBdr><w:top w:val=\"single\" w:sz=\"4\" w:color=\""
        + hex(p.border()) + "\"/></w:pBdr><w:tabs><w:tab w:val=\"right\" w:pos=\"9638\"/></w:tabs></w:pPr>"
        + run(esc(tituloDoc)) + "<w:r><w:rPr>" + corMuted() + "</w:rPr><w:tab/><w:t xml:space=\"preserve\">Página </w:t></w:r>"
        + campo("PAGE") + run(" de ") + campo("NUMPAGES") + "</w:p></w:ftr>";

    String contentTypes = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
        + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
        + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
        + "<Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>"
        + "<Override PartName=\"/word/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml\"/>"
        + "<Override PartName=\"/word/footer1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.footer+xml\"/>"
        + "</Types>";
    String rels = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
        + "<Relationship Id=\"rId1\" Type=\"" + R + "/officeDocument\" Target=\"word/document.xml\"/>"
        + "</Relationships>";
    String docRels = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
        + "<Relationship Id=\"rId1\" Type=\"" + R + "/styles\" Target=\"styles.xml\"/>"
        + "<Relationship Id=\"rId2\" Type=\"" + R + "/footer\" Target=\"footer1.xml\"/>"
        + "</Relationships>";

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (ZipOutputStream zip = new ZipOutputStream(out)) {
      arquivo(zip, "[Content_Types].xml", contentTypes);
      arquivo(zip, "_rels/.rels", rels);
      arquivo(zip, "word/_rels/document.xml.rels", docRels);
      arquivo(zip, "word/document.xml", documentXml);
      arquivo(zip, "word/styles.xml", estilos());
      arquivo(zip, "word/footer1.xml", footer);
    }
    return out.toByteArray();
  }

  /** styles.xml: Normal, Title, Subtitle, Heading1 e Heading2 com a fonte e as cores do tema. */
  private String estilos() {
    String fonte = "<w:rFonts w:ascii=\"" + p.docxFont() + "\" w:hAnsi=\"" + p.docxFont() + "\" w:cs=\""
        + p.docxFont() + "\"/>";
    String faixa = p.headerBg().equalsIgnoreCase("#ffffff") ? ""
        : "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"" + hex(p.headerBg()) + "\"/>";
    return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><w:styles xmlns:w=\"" + W + "\">"
        + "<w:docDefaults><w:rPrDefault><w:rPr>" + fonte + "<w:color w:val=\"" + hex(p.text())
        + "\"/><w:sz w:val=\"" + p.bodySize() * 2 + "\"/><w:lang w:val=\"pt-BR\"/></w:rPr></w:rPrDefault>"
        + "<w:pPrDefault><w:pPr><w:spacing w:after=\"120\" w:line=\"276\" w:lineRule=\"auto\"/></w:pPr></w:pPrDefault></w:docDefaults>"
        + "<w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\"><w:name w:val=\"Normal\"/></w:style>"
        + "<w:style w:type=\"paragraph\" w:styleId=\"Title\"><w:name w:val=\"Title\"/><w:basedOn w:val=\"Normal\"/>"
        + "<w:pPr><w:spacing w:before=\"0\" w:after=\"60\"/>" + faixa
        + "<w:pBdr><w:bottom w:val=\"single\" w:sz=\"24\" w:space=\"4\" w:color=\"" + hex(p.accent()) + "\"/></w:pBdr></w:pPr>"
        + "<w:rPr><w:b/><w:color w:val=\"" + hex(p.headerText()) + "\"/><w:sz w:val=\"" + (p.titleSize() + 6) * 2
        + "\"/></w:rPr></w:style>"
        + "<w:style w:type=\"paragraph\" w:styleId=\"Subtitle\"><w:name w:val=\"Subtitle\"/><w:basedOn w:val=\"Normal\"/>"
        + "<w:pPr><w:spacing w:after=\"240\"/></w:pPr><w:rPr><w:color w:val=\"" + hex(p.muted())
        + "\"/><w:sz w:val=\"24\"/></w:rPr></w:style>"
        + heading(1, p.headingSize() + 3) + heading(2, p.headingSize())
        + "</w:styles>";
  }

  private String heading(int n, int tamanho) {
    return "<w:style w:type=\"paragraph\" w:styleId=\"Heading" + n + "\"><w:name w:val=\"heading " + n
        + "\"/><w:basedOn w:val=\"Normal\"/><w:next w:val=\"Normal\"/><w:pPr><w:keepNext/>"
        + "<w:spacing w:before=\"320\" w:after=\"120\"/><w:pBdr><w:bottom w:val=\"single\" w:sz=\"8\" w:space=\"2\" w:color=\""
        + hex(p.accent()) + "\"/></w:pBdr><w:outlineLvl w:val=\"" + (n - 1) + "\"/></w:pPr><w:rPr><w:b/><w:color w:val=\""
        + hex(p.primary()) + "\"/><w:sz w:val=\"" + tamanho * 2 + "\"/></w:rPr></w:style>";
  }

  private String corMuted() {
    return "<w:color w:val=\"" + hex(p.muted()) + "\"/><w:sz w:val=\"16\"/>";
  }

  private String run(String t) {
    return "<w:r><w:rPr>" + corMuted() + "</w:rPr><w:t xml:space=\"preserve\">" + t + "</w:t></w:r>";
  }

  private String campo(String nome) {
    return "<w:fldSimple w:instr=\" " + nome + " \"><w:r><w:rPr>" + corMuted() + "</w:rPr><w:t>1</w:t></w:r></w:fldSimple>";
  }

  private static String hex(String cor) {
    return cor.substring(1).toUpperCase();
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
