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

    DestaqueMedia destaque = new DestaqueMedia();

    switch (formato) {
      case "markdown":
        // markdown e texto puro, entao aqui da pra usar os decorators das demos
        return texto(decorar(new MarkdownExporter(t).highlightRows(destaque), pucpr).export(doc),
            "text/markdown", "md");
      case "html":
        // o html agora e uma pagina completa; marca d'agua/paginas por linha estragariam o layout
        return texto(new HtmlExporter(t).highlightRows(destaque).export(doc), "text/html", "html");
      case "csv":
        // csv tambem nao passa pelos decorators, senao o Excel abre errado
        return texto(new CsvExporter().export(doc), "text/csv", "csv");
      case "pdf":
        // binario: nao passa pelos decorators de texto
        PdfExporter pdf = new PdfExporter(t);
        pdf.highlightRows(destaque);
        return new Arquivo(pdf.exportBytes(doc), "application/pdf", "pdf");
      case "docx":
        DocxExporter docx = new DocxExporter(t, aviso);
        docx.highlightRows(destaque);
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

  /** Formatos que aparecem na pagina: id, nome, icone, se e da lib e uma descricao curta. */
  private static final String[][] FORMATOS = {
    {"html", "HTML", "\uD83C\uDF10", "lib", "Pagina completa com o CSS do tema."},
    {"pdf", "PDF", "\uD83D\uDCC4", "lib", "PDF de verdade, feito na mao dentro da lib."},
    {"markdown", "Markdown", "\uD83D\uDCDD", "lib", "Texto com tabelas, com marca d'agua e paginas."},
    {"csv", "CSV", "\uD83D\uDCCA", "ext", "Separado por ; pra abrir direto no Excel."},
    {"docx", "DOCX", "\uD83D\uDCD8", "ext", "Word com estilos, tabela e rodape."},
  };

  private static String pagina(String tema) {
    boolean pucpr = tema.equals("pucpr");
    String cor = pucpr ? "#7a1a2b" : "#111111";
    String destaque = pucpr ? "#c9a227" : "#888888";
    String fundo = pucpr ? "#fbf8f8" : "#f4f4f2";
    String fonte = pucpr ? "'Segoe UI', Arial, Helvetica, sans-serif" : "Georgia, 'Times New Roman', serif";

    StringBuilder cards = new StringBuilder();
    for (String[] f : FORMATOS) {
      boolean lib = f[3].equals("lib");
      cards.append("<article class=\"card\"><div class=\"topo\"><span class=\"icone\">").append(f[2])
          .append("</span><span class=\"badge ").append(lib ? "b-lib" : "b-ext").append("\">")
          .append(lib ? "lib" : "extens&atilde;o").append("</span></div><h3>").append(f[1])
          .append("</h3><p>").append(f[4]).append("</p><div class=\"botoes\">")
          .append("<a class=\"btn\" href=\"/ver/").append(f[0]).append("?tema=").append(tema)
          .append("\" data-fmt=\"").append(f[0]).append("\" onclick=\"return ver(this)\">Visualizar</a>")
          .append("<a class=\"btn sec\" href=\"/baixar/").append(f[0]).append("?tema=").append(tema)
          .append("\">Baixar</a></div></article>\n");
    }

    return PAGINA
        .replace("{{COR}}", cor)
        .replace("{{DESTAQUE}}", destaque)
        .replace("{{FUNDO}}", fundo)
        .replace("{{FONTE}}", fonte)
        .replace("{{TEMA}}", tema)
        .replace("{{ATIVO_CLASSICO}}", pucpr ? "" : " ativo")
        .replace("{{ATIVO_PUCPR}}", pucpr ? " ativo" : "")
        .replace("{{CARDS}}", cards.toString());
  }

  private static final String PAGINA = """
      <!DOCTYPE html>
      <html lang="pt-BR"><head><meta charset="utf-8">
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <title>Boletim do Semestre</title>
      <style>
      :root{--cor:{{COR}};--dest:{{DESTAQUE}};--fundo:{{FUNDO}}}
      *{box-sizing:border-box}
      body{margin:0;font-family:{{FONTE}};background:var(--fundo);color:#222}
      header{background:var(--cor);color:#fff;padding:28px 0 22px;border-bottom:5px solid var(--dest)}
      .wrap{max-width:1180px;margin:0 auto;padding:0 24px}
      .cab{display:flex;justify-content:space-between;align-items:center;gap:16px;flex-wrap:wrap}
      header h1{margin:0;font-size:30px}
      header p{margin:4px 0 0;opacity:.8}
      .temas{display:flex;gap:8px;background:rgba(255,255,255,.12);padding:6px;border-radius:999px}
      .tema{display:flex;align-items:center;gap:8px;color:#fff;text-decoration:none;padding:8px 14px;
        border-radius:999px;font-size:14px;font-family:'Segoe UI',Arial,sans-serif}
      .tema.ativo{background:#fff;color:#222;font-weight:600}
      .bolinhas{display:flex}
      .bolinhas i{width:14px;height:14px;border-radius:50%;border:2px solid #fff;margin-left:-4px;display:block}
      main.wrap{padding-top:30px;padding-bottom:48px}
      h2{font-size:15px;text-transform:uppercase;letter-spacing:1px;color:#666;margin:0 0 12px}
      .grade{display:grid;grid-template-columns:repeat(auto-fill,minmax(205px,1fr));gap:16px;margin-bottom:28px}
      .card{background:#fff;border-radius:14px;padding:18px;box-shadow:0 1px 3px rgba(0,0,0,.08),0 6px 18px rgba(0,0,0,.05);
        display:flex;flex-direction:column;border-top:4px solid var(--cor);transition:transform .15s}
      .card:hover{transform:translateY(-3px)}
      .topo{display:flex;justify-content:space-between;align-items:center}
      .icone{font-size:30px}
      .badge{font-size:11px;font-weight:700;text-transform:uppercase;padding:3px 9px;border-radius:999px;
        font-family:'Segoe UI',Arial,sans-serif;letter-spacing:.5px}
      .b-lib{background:#e8eef8;color:#2a4a7f}
      .b-ext{background:#fdf1d8;color:#8a5a00}
      .card h3{margin:10px 0 4px;font-size:20px;color:var(--cor)}
      .card p{margin:0 0 14px;font-size:14px;color:#666;flex:1}
      .botoes{display:flex;gap:8px}
      .btn{flex:1;text-align:center;padding:8px 0;border-radius:8px;background:var(--cor);color:#fff;
        text-decoration:none;font-size:14px;font-family:'Segoe UI',Arial,sans-serif}
      .btn.sec{background:#fff;color:var(--cor);border:1px solid var(--cor)}
      .btn:hover{opacity:.85}
      .painel{background:#fff;border-radius:14px;box-shadow:0 1px 3px rgba(0,0,0,.08),0 6px 18px rgba(0,0,0,.05);overflow:hidden}
      .painel-topo{display:flex;justify-content:space-between;align-items:center;padding:12px 18px;
        border-bottom:1px solid #eee;font-family:'Segoe UI',Arial,sans-serif}
      .painel-topo b{color:var(--cor)}
      .painel-topo a{font-size:14px;color:var(--cor)}
      iframe{width:100%;height:720px;border:0;display:block;background:#fff}
      pre{margin:0;padding:20px;height:720px;overflow:auto;background:#1e1e1e;color:#e6e6e6;
        font:13px/1.5 Consolas,'Courier New',monospace;white-space:pre}
      .vazio{padding:80px 20px;text-align:center;color:#666;font-family:'Segoe UI',Arial,sans-serif}
      .vazio .btn{display:inline-block;padding:10px 22px;margin-top:12px}
      footer{text-align:center;color:#999;font-size:13px;padding:0 0 28px}
      @media (max-width:640px){header h1{font-size:24px}iframe,pre{height:480px}}
      </style></head><body>
      <header><div class="wrap cab">
        <div><h1>Boletim do Semestre</h1><p>Visualize ou baixe o boletim em v&aacute;rios formatos</p></div>
        <nav class="temas">
          <a class="tema{{ATIVO_CLASSICO}}" href="/?tema=classico"><span class="bolinhas"><i style="background:#000"></i><i style="background:#fff"></i><i style="background:#999"></i></span>Cl&aacute;ssico</a>
          <a class="tema{{ATIVO_PUCPR}}" href="/?tema=pucpr"><span class="bolinhas"><i style="background:#7a1a2b"></i><i style="background:#c9a227"></i><i style="background:#f6eaec"></i></span>PUCPR</a>
        </nav>
      </div></header>
      <main class="wrap">
        <h2>Formatos</h2>
        <section class="grade">
      {{CARDS}}    </section>
        <h2>Pr&eacute;via</h2>
        <section class="painel">
          <div class="painel-topo"><span>Visualizando: <b id="nome">HTML</b></span><a id="abrir" href="/ver/html?tema={{TEMA}}" target="_blank">abrir em nova aba &#8599;</a></div>
          <div id="previa"><iframe src="/ver/html?tema={{TEMA}}" title="previa"></iframe></div>
        </section>
      </main>
      <footer>Mini projeto de Design Patterns &middot; PUCPR</footer>
      <script>
      var tema = "{{TEMA}}";
      var nomes = {html:"HTML", pdf:"PDF", markdown:"Markdown", csv:"CSV", docx:"DOCX"};
      function ver(link) {
        var fmt = link.getAttribute("data-fmt");
        var url = "/ver/" + fmt + "?tema=" + tema;
        var previa = document.getElementById("previa");
        document.getElementById("nome").textContent = nomes[fmt];
        document.getElementById("abrir").href = url;
        if (fmt === "html" || fmt === "pdf") {
          previa.innerHTML = '<iframe src="' + url + '" title="previa"></iframe>';
        } else if (fmt === "docx") {
          previa.innerHTML = '<div class="vazio">O navegador n&atilde;o mostra DOCX direto.<br>' +
            '<a class="btn" href="/baixar/docx?tema=' + tema + '">Baixar DOCX</a></div>';
        } else {
          fetch(url).then(function (r) { return r.text(); }).then(function (t) {
            var pre = document.createElement("pre");
            pre.textContent = t;
            previa.innerHTML = "";
            previa.appendChild(pre);
          });
        }
        return false;
      }
      </script>
      </body></html>
      """;

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
