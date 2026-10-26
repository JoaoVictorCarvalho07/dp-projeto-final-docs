package br.pucpr.docs.theme;

/** Tema escuro/minimalista: sans-serif, títulos com marcador. */
public final class DarkThemeFactory implements ThemeFactory {
  @Override
  public String name() {
    return "dark";
  }

  @Override
  public TitleStyle createTitleStyle() {
    return new TitleStyle() {
      @Override
      public String css() {
        return "font-family: sans-serif; color: #eee; background: #111;";
      }

      @Override
      public String format(String text) {
        return "» " + text;
      }
    };
  }

  @Override
  public ParagraphStyle createParagraphStyle() {
    return () -> "font-family: sans-serif; color: #ddd; background: #111;";
  }

  @Override
  public TableStyle createTableStyle() {
    return new TableStyle() {
      @Override
      public String css() {
        return "border-collapse: collapse; color: #ddd; background: #222;";
      }

      @Override
      public String columnSeparator() {
        return " ; ";
      }
    };
  }
}
