package br.pucpr.example;

import br.pucpr.docs.builder.DocumentBuilder;
import br.pucpr.docs.decorator.PageNumberDecorator;
import br.pucpr.docs.decorator.WatermarkDecorator;
import br.pucpr.docs.export.DocumentExporter;
import br.pucpr.docs.export.HtmlExporter;
import br.pucpr.docs.export.MarkdownExporter;
import br.pucpr.docs.export.PdfTextExporter;
import br.pucpr.docs.model.Document;
import br.pucpr.docs.model.Node;
import br.pucpr.docs.prototype.TemplateRegistry;
import br.pucpr.docs.theme.DarkThemeFactory;

/** Demonstra os 8 patterns usando a biblioteca como um cliente externo. */
public class Main {
  public static void main(String[] args) {
    // Builder + Composite
    Document doc =
        DocumentBuilder.create("Relatório de Notas")
            .author("Grupo Design Patterns")
            .section("Introdução")
            .paragraph("Este documento foi montado com o Builder.")
            .section("Detalhes")
            .paragraph("Seções podem ser aninhadas (Composite).")
            .endSection()
            .endSection()
            .section("Notas")
            .table("Aluno", "Nota")
            .row("Ana", "9.5")
            .row("Bruno", "8.0")
            .endSection()
            .build();

    // Iterator
    System.out.println("== Iterator (nós em profundidade) ==");
    for (Node n : doc) {
      System.out.println(" - " + n.getClass().getSimpleName());
    }

    // Visitor + Template Method + Abstract Factory
    System.out.println("\n== Markdown (tema clássico) ==");
    System.out.println(new MarkdownExporter().export(doc));
    System.out.println("== HTML (tema escuro) ==");
    System.out.println(new HtmlExporter(new DarkThemeFactory()).export(doc));

    // Decorator
    System.out.println("== PDF-texto com marca d'água + numeração ==");
    DocumentExporter pdf =
        new PageNumberDecorator(new WatermarkDecorator(new PdfTextExporter(), "RASCUNHO"), 5);
    System.out.println(pdf.export(doc));

    // Open/Closed: exportador definido fora da lib
    System.out.println("== JSON (exportador do cliente) ==");
    System.out.println(new JsonExporter().export(doc));

    // Prototype
    TemplateRegistry templates = new TemplateRegistry();
    templates.register("relatorio", doc);
    Document copia = templates.create("relatorio");
    copia.setTitle("Cópia editada");
    System.out.println("\n== Prototype ==");
    System.out.println("Original: " + doc.getTitle() + " | Cópia: " + copia.getTitle());
  }
}
