package br.pucpr.docs.theme;

/** Tema clássico: serifado, títulos em maiúsculas. */
public final class ClassicThemeFactory implements ThemeFactory {
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
}
