package br.pucpr.docs;

import static org.junit.jupiter.api.Assertions.*;

import br.pucpr.docs.builder.DocumentBuilder;
import br.pucpr.docs.decorator.PageNumberDecorator;
import br.pucpr.docs.decorator.WatermarkDecorator;
import br.pucpr.docs.export.AbstractExporter;
import br.pucpr.docs.export.HtmlExporter;
import br.pucpr.docs.export.MarkdownExporter;
import br.pucpr.docs.export.PdfTextExporter;
import br.pucpr.docs.model.Document;
import br.pucpr.docs.model.Node;
import br.pucpr.docs.model.Paragraph;
import br.pucpr.docs.model.Section;
import br.pucpr.docs.model.Table;
import br.pucpr.docs.prototype.TemplateRegistry;
import br.pucpr.docs.theme.ClassicThemeFactory;
import br.pucpr.docs.theme.DarkThemeFactory;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PatternsTest {

  @Test
  void builderMontaArvore() {
    Document d = Fixtures.sample();
    assertEquals("Relatório", d.getTitle());
    assertEquals(2, d.getRoot().getChildren().size());
    assertThrows(IllegalStateException.class, () -> DocumentBuilder.create("x").endSection());
    assertThrows(IllegalStateException.class, () -> DocumentBuilder.create("x").row("a"));
  }

  @Test
  void compositeAninhaSecoes() {
    Section intro = (Section) Fixtures.sample().getRoot().getChildren().get(0);
    assertInstanceOf(Paragraph.class, intro.getChildren().get(0));
    assertInstanceOf(Section.class, intro.getChildren().get(1));
  }

  @Test
  void iteratorPercorreEmProfundidade() {
    List<String> nomes = new ArrayList<>();
    for (Node n : Fixtures.sample()) {
      nomes.add(n.getClass().getSimpleName());
    }
    assertEquals(
        List.of("Section", "Section", "Paragraph", "Section", "Paragraph", "Section", "Table"),
        nomes);
  }

  @Test
  void visitorTemplateMethodGeramFormatos() {
    Document d = Fixtures.sample();
    String md = new MarkdownExporter().export(d);
    assertTrue(md.startsWith("# RELATÓRIO"));
    assertTrue(md.contains("### SUB"));
    assertTrue(md.contains("| Ana | 10 |"));
    String html = new HtmlExporter().export(d);
    assertTrue(html.contains("<td>Ana</td>"));
    assertTrue(html.endsWith("</html>\n"));
    assertTrue(new PdfTextExporter().export(d).startsWith("%PDF-simplificado"));
  }

  @Test
  void visitorAceitaExportadorNovoSemMudarLib() {
    AbstractExporter contador =
        new AbstractExporter() {
          @Override
          protected void writeHeader(Document d) {}

          @Override
          protected void writeSectionTitle(Section s, int level) {
            out.append('S');
          }

          @Override
          public void visitParagraph(Paragraph p) {
            out.append('P');
          }

          @Override
          public void visitTable(Table t) {
            out.append('T');
          }
        };
    assertEquals("SPSPST", contador.export(Fixtures.sample()));
  }

  @Test
  void abstractFactoryTrocaTema() {
    Document d = Fixtures.sample();
    assertTrue(new MarkdownExporter(new ClassicThemeFactory()).export(d).contains("RELATÓRIO"));
    assertTrue(new MarkdownExporter(new DarkThemeFactory()).export(d).contains("» Relatório"));
    assertTrue(new HtmlExporter(new DarkThemeFactory()).export(d).contains("background: #111"));
  }

  @Test
  void decoratorEmpilha() {
    var exporter = new PageNumberDecorator(new WatermarkDecorator(new PdfTextExporter(), "RASCUNHO"), 3);
    String out = exporter.export(Fixtures.sample());
    assertTrue(out.startsWith("[MARCA D'ÁGUA: RASCUNHO]"));
    assertTrue(out.contains("--- Página 1/"));
    assertThrows(IllegalArgumentException.class, () -> new PageNumberDecorator(new HtmlExporter(), 0));
  }

  @Test
  void pageNumberNaoDeixaLinhaOrfa() {
    br.pucpr.docs.export.DocumentExporter fake = d -> "a\nb\nc\n";
    String out = new PageNumberDecorator(fake, 2).export(Fixtures.sample());
    assertEquals("a\nb\nc\n--- Página 1/1 ---\n", out);
  }

  @Test
  void prototypeFazCopiaProfunda() {
    TemplateRegistry reg = new TemplateRegistry();
    reg.register("relatorio", Fixtures.sample());
    Document a = reg.create("relatorio");
    Section intro = (Section) a.getRoot().getChildren().get(0);
    ((Paragraph) intro.getChildren().get(0)).setText("Alterado");
    a.setTitle("Outro");
    Document b = reg.create("relatorio");
    assertEquals("Relatório", b.getTitle());
    Section introB = (Section) b.getRoot().getChildren().get(0);
    assertEquals("Olá", ((Paragraph) introB.getChildren().get(0)).getText());
    assertThrows(IllegalArgumentException.class, () -> reg.create("nao-existe"));
  }
}
