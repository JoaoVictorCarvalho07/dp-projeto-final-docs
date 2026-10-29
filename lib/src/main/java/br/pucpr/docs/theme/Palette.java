package br.pucpr.docs.theme;

/**
 * Produto da família de estilos: a identidade visual do tema (cores, fontes e tamanhos). Todos os
 * exportadores leem daqui, então HTML, PDF e DOCX saem com a mesma cara.
 *
 * <p>Cores em hex ("#7a1a2b"). {@code zebra} pode ser null (sem linhas alternadas).
 */
public record Palette(
    String cssFont,
    String docxFont,
    PdfFont pdfFont,
    String text,
    String muted,
    String background,
    String primary,
    String accent,
    String headerBg,
    String headerText,
    String tableHeaderBg,
    String tableHeaderText,
    String zebra,
    String border,
    String positive,
    String negative,
    int titleSize,
    int headingSize,
    int bodySize) {

  /** Fontes padrão do PDF (as 14 que todo leitor tem). */
  public enum PdfFont {
    HELVETICA("Helvetica", "Helvetica-Bold"),
    TIMES("Times-Roman", "Times-Bold");

    public final String regular;
    public final String bold;

    PdfFont(String regular, String bold) {
      this.regular = regular;
      this.bold = bold;
    }
  }

  /** Paleta neutra usada por temas que não definem a sua. */
  public static Palette neutra() {
    return new Palette(
        "Arial, Helvetica, sans-serif", "Arial", PdfFont.HELVETICA,
        "#222222", "#666666", "#ffffff", "#222222", "#555555",
        "#ffffff", "#222222", "#eeeeee", "#222222", null, "#999999",
        "#2e7d32", "#c62828", 22, 15, 11);
  }
}
