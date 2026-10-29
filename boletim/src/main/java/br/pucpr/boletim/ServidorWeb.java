package br.pucpr.boletim;

import br.pucpr.docs.decorator.PageNumberDecorator;
import br.pucpr.docs.decorator.WatermarkDecorator;
import br.pucpr.docs.export.DocumentExporter;
import br.pucpr.docs.export.HtmlExporter;
import br.pucpr.docs.export.MarkdownExporter;
import br.pucpr.docs.export.PdfExporter;
import br.pucpr.docs.model.Document;
import br.pucpr.docs.theme.ClassicThemeFactory;
import br.pucpr.docs.theme.ThemeFactory;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Servidor web simples (HttpServer do proprio JDK) pra ver ou baixar o boletim.
 * Rotas: GET /  |  GET /ver/{formato}  |  GET /baixar/{formato}   (?tema=classico ou pucpr)
 */
public class ServidorWeb {
  private static final int PORTA = 8080;
  private static Path csv;

  public static void main(String[] args) throws IOException {
    csv = LeitorCsv.acharArquivo(args.length > 0 ? args[0] : "notas.csv");
    HttpServer server = HttpServer.create(new InetSocketAddress(PORTA), 0);
    server.createContext("/", ServidorWeb::tratar);
    server.start();
    System.out.println("Boletim no ar: http://localhost:" + PORTA);
  }

  /** Resultado de uma exportacao: bytes + tipo + extensao do arquivo. */
  record Arquivo(byte[] bytes, String contentType, String extensao) {}

  static Arquivo exportar(String formato, String tema, Document doc) {
    boolean pucpr = "pucpr".equals(tema);
    ThemeFactory t = pucpr ? new PucprThemeFactory() : new ClassicThemeFactory();
    String aviso = pucpr ? "CONFIDENCIAL" : "RASCUNHO";

    switch (formato) {
      case "markdown":
        return texto(decorar(new MarkdownExporter(t), pucpr).export(doc), "text/markdown", "md");
      case "html":
        return texto(decorar(new HtmlExporter(t), pucpr).export(doc), "text/html", "html");
      case "csv":
        // csv tambem nao passa pelos decorators, senao o Excel abre errado
        return texto(new CsvExporter().export(doc), "text/csv", "csv");
      case "pdf":
        // binario: nao passa pelos decorators de texto
        return new Arquivo(new PdfExporter(t).exportBytes(doc), "application/pdf", "pdf");
      case "docx":
        DocxExporter docx = new DocxExporter(t, aviso);
        return new Arquivo(docx.export(doc), docx.contentType(), docx.extensao());
      default:
        return null;
    }
  }

  /** Mesmos decorators das demos: classico = marca d'agua, pucpr = confidencial. */
  private static DocumentExporter decorar(DocumentExporter base, boolean pucpr) {
    DocumentExporter d = pucpr ? new ConfidencialDecorator(base) : new WatermarkDecorator(base, "RASCUNHO");
    return new PageNumberDecorator(d, 40);
  }

  private static Arquivo texto(String s, String tipo, String ext) {
    return new Arquivo(s.getBytes(StandardCharsets.UTF_8), tipo + "; charset=utf-8", ext);
  }

  private static void tratar(HttpExchange ex) throws IOException {
    try {
      String caminho = ex.getRequestURI().getPath();
      String tema = "pucpr".equals(param(ex, "tema")) ? "pucpr" : "classico";

      if (!"GET".equals(ex.getRequestMethod())) {
        responder(ex, 405, "text/plain; charset=utf-8", "metodo nao suportado".getBytes(StandardCharsets.UTF_8));
        return;
      }
      if (caminho.equals("/")) {
        responder(ex, 200, "text/html; charset=utf-8", pagina(tema).getBytes(StandardCharsets.UTF_8));
        return;
      }

      String[] partes = caminho.split("/");
      if (partes.length == 3 && (partes[1].equals("ver") || partes[1].equals("baixar"))) {
        Document doc = GeradorBoletim.gerar("Joao Victor", LeitorCsv.ler(csv));
        Arquivo arq = exportar(partes[2], tema, doc);
        if (arq != null) {
          String nome = "boletim-" + tema + "." + arq.extensao();
          String disp = partes[1].equals("baixar") ? "attachment" : "inline";
          // markdown/csv aparecem como texto no navegador na hora de visualizar
          String tipo = disp.equals("inline")
              && (arq.extensao().equals("md") || arq.extensao().equals("csv"))
              ? "text/plain; charset=utf-8" : arq.contentType();
          ex.getResponseHeaders().set("Content-Disposition", disp + "; filename=\"" + nome + "\"");
          responder(ex, 200, tipo, arq.bytes());
          return;
        }
      }
      responder(ex, 404, "text/plain; charset=utf-8", "nao encontrado".getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      e.printStackTrace();
      responder(ex, 500, "text/plain; charset=utf-8", ("erro: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
    }
  }

  private static String pagina(String tema) {
    StringBuilder h = new StringBuilder();
    h.append("<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>Boletim</title>");
    h.append("<style>body{font-family:sans-serif;max-width:600px;margin:40px auto}")
        .append("li{margin:8px 0}a{margin-left:10px}</style></head><body>");
    h.append("<h1>Boletim do Semestre</h1>");
    h.append("<p>Tema: <b>").append(tema).append("</b> | trocar: ")
        .append("<a href=\"/?tema=classico\">classico</a><a href=\"/?tema=pucpr\">pucpr</a></p>");
    h.append("<h2>Formatos da lib</h2><ul>");
    item(h, "Markdown", "markdown", tema);
    item(h, "HTML", "html", tema);
    item(h, "PDF", "pdf", tema);
    h.append("</ul><h2>Extens&otilde;es do boletim</h2><ul>");
    item(h, "CSV", "csv", tema);
    item(h, "DOCX", "docx", tema);
    h.append("</ul></body></html>");
    return h.toString();
  }

  private static void item(StringBuilder h, String nome, String fmt, String tema) {
    h.append("<li>").append(nome)
        .append("<a href=\"/ver/").append(fmt).append("?tema=").append(tema).append("\">Visualizar</a>")
        .append("<a href=\"/baixar/").append(fmt).append("?tema=").append(tema).append("\">Baixar</a></li>");
  }

  private static String param(HttpExchange ex, String nome) {
    String q = ex.getRequestURI().getQuery();
    if (q == null) {
      return null;
    }
    for (String par : q.split("&")) {
      String[] kv = par.split("=", 2);
      if (kv[0].equals(nome) && kv.length == 2) {
        return kv[1];
      }
    }
    return null;
  }

  private static void responder(HttpExchange ex, int status, String tipo, byte[] corpo) throws IOException {
    ex.getResponseHeaders().set("Content-Type", tipo);
    ex.sendResponseHeaders(status, corpo.length);
    try (OutputStream os = ex.getResponseBody()) {
      os.write(corpo);
    }
  }
}
