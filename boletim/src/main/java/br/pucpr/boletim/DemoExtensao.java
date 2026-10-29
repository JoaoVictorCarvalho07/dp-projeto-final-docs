package br.pucpr.boletim;

import br.pucpr.docs.decorator.PageNumberDecorator;
import br.pucpr.docs.export.DocumentExporter;
import br.pucpr.docs.export.HtmlExporter;
import br.pucpr.docs.model.Document;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * Caso de extensao (Open/Closed): CsvExporter, PucprThemeFactory e ConfidencialDecorator foram
 * criados aqui no boletim. A lib continua igual.
 */
public class DemoExtensao {
  public static void main(String[] args) throws Exception {
    Path csv = LeitorCsv.acharArquivo(args.length > 0 ? args[0] : "notas.csv");
    Document doc = GeradorBoletim.gerar("Joao Victor", LeitorCsv.ler(csv));

    PucprThemeFactory tema = new PucprThemeFactory();

    // exportador novo (sem decorator, pra nao estragar o csv)
    DocumentExporter csvExp = new CsvExporter();

    // exportador da lib usando o tema novo + decorator novo + decorator da lib
    DocumentExporter html =
        new PageNumberDecorator(new ConfidencialDecorator(new HtmlExporter(tema)), 40);

    System.out.println("Escolha o formato do boletim:");
    System.out.println("1 - CSV (exportador novo, abre no Excel)");
    System.out.println("2 - HTML da lib com tema PUCPR");
    System.out.print("> ");
    String opcao = new Scanner(System.in).nextLine().trim();

    switch (opcao) {
      case "1" -> System.out.println(csvExp.export(doc));
      case "2" -> System.out.println(html.export(doc));
      default -> System.out.println("Opcao invalida: " + opcao);
    }
  }
}
