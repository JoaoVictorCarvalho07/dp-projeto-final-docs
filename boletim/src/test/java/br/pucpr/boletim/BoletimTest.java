package br.pucpr.boletim;

import static org.junit.jupiter.api.Assertions.*;

import br.pucpr.docs.export.HtmlExporter;
import br.pucpr.docs.export.MarkdownExporter;
import br.pucpr.docs.model.Document;
import java.util.List;
import org.junit.jupiter.api.Test;

class BoletimTest {

  private Document doc() {
    List<Materia> materias = LeitorCsv.lerLinhas(List.of(
        "materia,professor,nota1,nota2",
        "Design Patterns,Prof. X,8.0,9.0",
        "Calculo,Prof. Y,5.0,6.0"));
    return GeradorBoletim.gerar("Aluno Teste", materias);
  }

  @Test
  void leCsvECalculaMedia() {
    List<Materia> m = LeitorCsv.lerLinhas(List.of("cabecalho", "BD,Prof,6.0,8.0", ""));
    assertEquals(1, m.size());
    assertEquals(7.0, m.get(0).media(), 0.001);
    assertTrue(m.get(0).aprovado());
  }

  @Test
  void boletimTemUmaSecaoPorMateriaMaisResumo() {
    String md = new MarkdownExporter().export(doc());
    assertTrue(md.contains("## RESUMO"));
    assertTrue(md.contains("## DESIGN PATTERNS"));
    assertTrue(md.contains("## CALCULO"));
    assertTrue(md.contains("| Media | 8.5 |"));
    assertTrue(md.contains("Aprovado em 1"));
  }

  @Test
  void csvExporterGeraLinhasComPontoEVirgula() {
    String csv = new CsvExporter().export(doc());
    String[] linhas = csv.split("\n");
    assertEquals("\uFEFFMateria;Professor;Avaliacao;Nota", linhas[0]);
    assertTrue(csv.contains("Design Patterns;Prof. X;N1;8.0\n"));
    assertTrue(csv.contains("Design Patterns;Prof. X;Media;8.5\n"));
    assertEquals("Resumo;;Media geral;7.0", linhas[linhas.length - 1]);
  }

  @Test
  void temaPucprFuncionaNoHtmlDaLib() {
    String html = new HtmlExporter(new PucprThemeFactory()).export(doc());
    assertTrue(html.contains("#7a1a2b"));
  }

  @Test
  void confidencialFicaNoTopo() {
    String out = new ConfidencialDecorator(new MarkdownExporter()).export(doc());
    assertTrue(out.startsWith("*** CONFIDENCIAL"));
  }

  @Test
  void pdfDaLibComecaComPdf() {
    byte[] pdf = new br.pucpr.docs.export.PdfExporter(new PucprThemeFactory()).exportBytes(doc());
    assertEquals("%PDF", new String(pdf, 0, 4, java.nio.charset.StandardCharsets.ISO_8859_1));
  }

  @Test
  void docxEhUmZipComDocumentXml() throws Exception {
    byte[] docx = new DocxExporter(new PucprThemeFactory(), "CONFIDENCIAL").export(doc());
    String xml = null;
    try (var zip = new java.util.zip.ZipInputStream(new java.io.ByteArrayInputStream(docx))) {
      java.util.zip.ZipEntry e;
      while ((e = zip.getNextEntry()) != null) {
        if (e.getName().equals("word/document.xml")) {
          xml = new String(zip.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
      }
    }
    assertNotNull(xml);
    assertTrue(xml.contains("Design Patterns"));
    assertTrue(xml.contains("CONFIDENCIAL"));
  }

  @Test
  void servidorConheceTodosOsFormatos() {
    for (String f : new String[] {"markdown", "html", "csv", "pdf", "docx"}) {
      assertNotNull(ServidorWeb.exportar(f, "classico", doc()), f);
    }
    assertNull(ServidorWeb.exportar("xls", "classico", doc()));
  }

  @Test
  void destaqueMediaVerdeOuVermelho() {
    DestaqueMedia d = new DestaqueMedia();
    assertEquals(br.pucpr.docs.export.RowHighlighter.Highlight.POSITIVE, d.highlight(List.of(), List.of("Media", "8.5")));
    assertEquals(br.pucpr.docs.export.RowHighlighter.Highlight.NEGATIVE, d.highlight(List.of(), List.of("Media", "5.5")));
    assertEquals(br.pucpr.docs.export.RowHighlighter.Highlight.NONE, d.highlight(List.of(), List.of("N1", "5.5")));
  }

  @Test
  void docxTemEstilosERodape() throws Exception {
    byte[] docx = new DocxExporter(new PucprThemeFactory(), null).export(doc());
    java.util.Set<String> nomes = new java.util.HashSet<>();
    String estilos = "";
    try (var zip = new java.util.zip.ZipInputStream(new java.io.ByteArrayInputStream(docx))) {
      java.util.zip.ZipEntry e;
      while ((e = zip.getNextEntry()) != null) {
        nomes.add(e.getName());
        if (e.getName().equals("word/styles.xml")) {
          estilos = new String(zip.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
      }
    }
    assertTrue(nomes.contains("word/footer1.xml"));
    assertTrue(estilos.contains("Heading1"));
    assertTrue(estilos.contains("7A1A2B"));
  }

  @Test
  void temaPucprEhDiferenteDoClassico() {
    var pucpr = new PucprThemeFactory().createPalette();
    var classico = new br.pucpr.docs.theme.ClassicThemeFactory().createPalette();
    assertEquals("#7a1a2b", pucpr.primary());
    assertNotNull(pucpr.zebra());
    assertNotEquals(classico.pdfFont(), pucpr.pdfFont());
  }
}
