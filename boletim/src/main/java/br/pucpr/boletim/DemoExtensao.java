package br.pucpr.boletim;

import br.pucpr.docs.decorator.PageNumberDecorator;
import br.pucpr.docs.export.DocumentExporter;
import br.pucpr.docs.export.HtmlExporter;
import br.pucpr.docs.model.Document;
import java.nio.file.Path;

/**
 * Caso de extensao (Open/Closed): LatexExporter, PucprThemeFactory e ConfidencialDecorator foram
 * criados aqui no boletim. A lib continua igual.
 */
public class DemoExtensao {
  public static void main(String[] args) throws Exception {
    Path csv = LeitorCsv.acharArquivo(args.length > 0 ? args[0] : "notas.csv");
    Document doc = GeradorBoletim.gerar("Joao Victor", LeitorCsv.ler(csv));

    PucprThemeFactory tema = new PucprThemeFactory();

    // exportador novo + tema novo + decorator novo, misturado com um decorator da lib
    DocumentExporter latex =
        new PageNumberDecorator(new ConfidencialDecorator(new LatexExporter(tema)), 40);

    // exportador da lib usando o tema novo
    DocumentExporter html = new ConfidencialDecorator(new HtmlExporter(tema));

    System.out.println("===== LaTeX (tema PUCPR) =====");
    System.out.println(latex.export(doc));
    System.out.println("===== HTML da lib com tema PUCPR =====");
    System.out.println(html.export(doc));
  }
}
