package br.pucpr.docs.theme;

/** Tema clássico: serifado, preto no branco, sóbrio. Títulos em maiúsculas. */
public final class ClassicThemeFactory implements ThemeFactory {
  private static final Palette PALETA = new Palette(
      "Georgia, 'Times New Roman', Times, serif", "Times New Roman", Palette.PdfFont.TIMES,
      "#111111", "#555555", "#ffffff", "#000000", "#333333",
      "#ffffff", "#000000", "#e6e6e6", "#000000", null, "#000000",
      "#1b5e20", "#b71c1c", 24, 15, 11);

  @Override
  public String name() {
    return "classic";
  }

  @Override
  public TitleStyle createTitleStyle() {
    return new TitleStyle() {
      @Override
      public String css() {
        return "font-family: Georgia, serif; color: #222;";
      }

      @Override
      public String format(String text) {
        return text.toUpperCase();
      }
    };
  }

  @Override
  public ParagraphStyle createParagraphStyle() {
    return () -> "font-family: Georgia, serif; line-height: 1.5;";
  }

  @Override
  public TableStyle createTableStyle() {
    return new TableStyle() {
      @Override
      public String css() {
        return "border-collapse: collapse; border: 1px solid #222;";
      }

      @Override
      public String columnSeparator() {
        return " | ";
      }
    };
  }

  @Override
  public Palette createPalette() {
    return PALETA;
  }
}
