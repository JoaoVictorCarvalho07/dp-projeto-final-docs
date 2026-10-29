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
  void latexExporterGeraTabela() {
    String tex = new LatexExporter().export(doc());
    assertTrue(tex.startsWith("\\documentclass{article}"));
    assertTrue(tex.contains("N1 & 8.0 \\\\"));
    assertTrue(tex.trim().endsWith("\\end{document}"));
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
}
