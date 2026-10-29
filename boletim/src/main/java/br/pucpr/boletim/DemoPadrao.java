package br.pucpr.boletim;

import br.pucpr.docs.decorator.PageNumberDecorator;
import br.pucpr.docs.decorator.WatermarkDecorator;
import br.pucpr.docs.export.DocumentExporter;
import br.pucpr.docs.export.HtmlExporter;
import br.pucpr.docs.export.MarkdownExporter;
import br.pucpr.docs.model.Document;
import br.pucpr.docs.theme.ClassicThemeFactory;
import java.nio.file.Path;

/** Caso padrao: usa so o que ja vem na lib, sem criar nenhuma classe nova. */
public class DemoPadrao {
  public static void main(String[] args) throws Exception {
    Path csv = Path.of(args.length > 0 ? args[0] : "notas.csv");
    Document doc = GeradorBoletim.gerar("Joao Victor", LeitorCsv.ler(csv));

    ClassicThemeFactory tema = new ClassicThemeFactory();

    DocumentExporter md =
        new PageNumberDecorator(new WatermarkDecorator(new MarkdownExporter(tema), "RASCUNHO"), 20);
    DocumentExporter html =
        new PageNumberDecorator(new WatermarkDecorator(new HtmlExporter(tema), "RASCUNHO"), 20);

    System.out.println("===== Markdown =====");
    System.out.println(md.export(doc));
    System.out.println("===== HTML =====");
    System.out.println(html.export(doc));
  }
}
