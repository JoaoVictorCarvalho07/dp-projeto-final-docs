package br.pucpr.docs.theme;

/**
 * ABSTRACT FACTORY: cria uma família de estilos que combinam entre si. Um tema novo é só uma
 * fábrica nova; os exportadores não mudam (Open/Closed).
 */
public interface ThemeFactory {
  String name();

  TitleStyle createTitleStyle();

  ParagraphStyle createParagraphStyle();

  TableStyle createTableStyle();

  /**
   * Cores, fontes e tamanhos do tema. É default pra não quebrar quem já implementava a interface
   * (temas antigos ganham a paleta neutra).
   */
  default Palette createPalette() {
    return Palette.neutra();
  }
}
