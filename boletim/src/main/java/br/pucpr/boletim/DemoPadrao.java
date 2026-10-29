package br.pucpr.boletim;

import br.pucpr.docs.decorator.PageNumberDecorator;
import br.pucpr.docs.decorator.WatermarkDecorator;
import br.pucpr.docs.export.DocumentExporter;
import br.pucpr.docs.export.HtmlExporter;
import br.pucpr.docs.export.MarkdownExporter;
import br.pucpr.docs.model.Document;
import br.pucpr.docs.theme.ClassicThemeFactory;
import java.nio.file.Path;
import java.util.Scanner;

/** Caso padrao: usa so o que ja vem na lib, sem criar nenhuma classe nova. */
public class DemoPadrao {
  public static void main(String[] args) throws Exception {
    Path csv = LeitorCsv.acharArquivo(args.length > 0 ? args[0] : "notas.csv");
    Document doc = GeradorBoletim.gerar("Joao Victor", LeitorCsv.ler(csv));

    ClassicThemeFactory tema = new ClassicThemeFactory();

    DocumentExporter md =
        new PageNumberDecorator(new WatermarkDecorator(new MarkdownExporter(tema), "RASCUNHO"), 20);
    DocumentExporter html =
        new PageNumberDecorator(new WatermarkDecorator(new HtmlExporter(tema), "RASCUNHO"), 20);

    System.out.println("Escolha o formato do boletim:");
    System.out.println("1 - Markdown");
    System.out.println("2 - HTML");
    System.out.print("> ");
    String opcao = new Scanner(System.in).nextLine().trim();

    switch (opcao) {
      case "1" -> System.out.println(md.export(doc));
      case "2" -> System.out.println(html.export(doc));
      default -> System.out.println("Opcao invalida: " + opcao);
    }
  }
}
