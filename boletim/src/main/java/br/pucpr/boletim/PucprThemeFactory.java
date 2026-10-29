package br.pucpr.boletim;

import br.pucpr.docs.theme.Palette;
import br.pucpr.docs.theme.ParagraphStyle;
import br.pucpr.docs.theme.TableStyle;
import br.pucpr.docs.theme.ThemeFactory;
import br.pucpr.docs.theme.TitleStyle;

/**
 * Tema novo com a identidade da PUCPR: sans-serif, vinho #7a1a2b com detalhe dourado, cabecalho
 * de tabela colorido e linhas zebradas. Nao precisou mexer em nenhum exportador.
 */
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
        return text;
      }
    };
  }

  @Override
  public ParagraphStyle createParagraphStyle() {
    return () -> "font-family: Arial, Helvetica, sans-serif; color: #2b2b2b;";
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

  @Override
  public Palette createPalette() {
    return new Palette(
        "'Segoe UI', Arial, Helvetica, sans-serif", "Arial", Palette.PdfFont.HELVETICA,
        "#2b2b2b", "#7d6b6e", "#fbf8f8", "#7a1a2b", "#c9a227",
        "#7a1a2b", "#ffffff", "#7a1a2b", "#ffffff", "#f6eaec", "#e2c6cc",
        "#2e7d32", "#c62828", 22, 15, 11);
  }
}
