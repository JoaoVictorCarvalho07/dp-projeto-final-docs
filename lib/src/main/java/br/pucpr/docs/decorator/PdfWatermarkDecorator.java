package br.pucpr.docs.decorator;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DECORATOR de byte[]: o PDF da lib é escrito na mão, sem PDFBox. O texto vai em diagonal, cinza
 * e semitransparente, no centro de cada página. Depois o xref é refeito, senão o arquivo quebra.
 */
public final class PdfWatermarkDecorator {
  private final String text;

  public PdfWatermarkDecorator(String text) {
    this.text = text;
  }

  public byte[] decorate(byte[] pdf) {
    String s = new String(pdf, StandardCharsets.ISO_8859_1);
    s = s.replace(
        "/Font << /F1 3 0 R /F2 4 0 R >>",
        "/Font << /F1 3 0 R /F2 4 0 R >> /ExtGState << /GS1 << /Type /ExtGState /ca 0.2 /CA 0.2 >> >>");
    s = injetar(s, desenho());
    // "startxref" tambem contem "xref", entao o corte e na linha do indice
    int corte = s.lastIndexOf("\nxref\n");
    String corpo = corte >= 0 ? s.substring(0, corte + 1) : s;
    return montar(corpo).getBytes(StandardCharsets.ISO_8859_1);
  }

  /** Acrescenta o desenho no fim de cada stream e atualiza o /Length. */
  private static String injetar(String pdf, String marca) {
    StringBuilder sb = new StringBuilder();
    int i = 0;
    while (true) {
      int lenPos = pdf.indexOf("<< /Length ", i);
      if (lenPos < 0) {
        sb.append(pdf.substring(i));
        break;
      }
      int numStart = lenPos + "<< /Length ".length();
      int numEnd = pdf.indexOf(" >>", numStart);
      int contentStart = pdf.indexOf("\nstream\n", numEnd) + "\nstream\n".length();
      int contentEnd = pdf.indexOf("\nendstream", contentStart);
      String content = pdf.substring(contentStart, contentEnd);
      if (!content.endsWith("\n")) {
        content += "\n";
      }
      content += marca;
      sb.append(pdf, i, numStart);
      sb.append(content.length());
      sb.append(pdf, numEnd, contentStart);
      sb.append(content);
      i = contentEnd;
    }
    return sb.toString();
  }

  /** Texto rotacionado -30 graus, cinza, com opacidade baixa, no meio da folha A4. */
  private String desenho() {
    int tam = 56;
    double ang = Math.toRadians(-30);
    double cos = Math.cos(ang);
    double sin = Math.sin(ang);
    double meio = text.length() * tam * 0.50 / 2.0;
    double tx = 297.5 - meio * cos;
    double ty = 421 - meio * sin;
    return "q\n"
        + "/GS1 gs\n"
        + "0.45 0.45 0.45 rg\n"
        + "BT\n"
        + "/F2 " + tam + " Tf\n"
        + n(cos) + " " + n(sin) + " " + n(-sin) + " " + n(cos) + " " + n(tx) + " " + n(ty) + " Tm\n"
        + "(" + esc(text) + ") Tj\n"
        + "ET\n"
        + "Q\n";
  }

  private static String montar(String corpo) {
    if (!corpo.endsWith("\n")) {
      corpo += "\n";
    }
    List<Integer> offs = new ArrayList<>();
    Matcher m = Pattern.compile("(?m)^\\d+ 0 obj$").matcher(corpo);
    while (m.find()) {
      offs.add(m.start());
    }
    StringBuilder sb = new StringBuilder(corpo);
    int xref = sb.length();
    sb.append("xref\n0 ").append(offs.size() + 1).append('\n');
    sb.append("0000000000 65535 f \n");
    for (int off : offs) {
      sb.append(String.format("%010d 00000 n \n", off));
    }
    sb.append("trailer\n<< /Size ").append(offs.size() + 1).append(" /Root 1 0 R >>\n");
    sb.append("startxref\n").append(xref).append("\n%%EOF\n");
    return sb.toString();
  }

  private static String n(double v) {
    return String.format(Locale.US, "%.2f", v);
  }

  private static String esc(String s) {
    StringBuilder sb = new StringBuilder();
    for (char ch : s.toCharArray()) {
      if (ch == '(' || ch == ')' || ch == '\\') {
        sb.append('\\').append(ch);
      } else if (ch <= 255) {
        sb.append(ch);
      } else {
        sb.append('?');
      }
    }
    return sb.toString();
  }
}
