package br.pucpr.boletim;

import br.pucpr.docs.theme.ParagraphStyle;
import br.pucpr.docs.theme.TableStyle;
import br.pucpr.docs.theme.ThemeFactory;
import br.pucpr.docs.theme.TitleStyle;

/** Tema novo com as cores da PUCPR. Nao precisou mexer em nenhum exportador. */
public class PucprThemeFactory implements ThemeFactory {

  @Override
  public String name() {
    return "pucpr";
  }

  @Override
  public TitleStyle createTitleStyle() {
    return new TitleStyle() {
      @Override
      public String css() {
        return "font-family: Arial, sans-serif; color: #7a1a2b;";
      }

      @Override
      public String format(String text) {
        return "PUCPR - " + text;
      }
    };
  }

  @Override
  public ParagraphStyle createParagraphStyle() {
    return () -> "font-family: Arial, sans-serif; color: #333;";
  }

  @Override
  public TableStyle createTableStyle() {
    return new TableStyle() {
      @Override
      public String css() {
        return "border-collapse: collapse; border: 2px solid #7a1a2b;";
      }

      @Override
      public String columnSeparator() {
        return " | ";
      }
    };
  }
}
