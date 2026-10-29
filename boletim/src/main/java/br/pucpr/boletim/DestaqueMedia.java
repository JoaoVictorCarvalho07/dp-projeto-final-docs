package br.pucpr.boletim;

import br.pucpr.docs.export.RowHighlighter;
import java.util.List;

/** Pinta a linha "Media": verde se passou (>= 7), vermelho se reprovou. */
public class DestaqueMedia implements RowHighlighter {

  @Override
  public Highlight highlight(List<String> headers, List<String> row) {
    if (row.isEmpty() || !row.get(0).equals("Media") || row.size() < 2) {
      return Highlight.NONE;
    }
    try {
      return Double.parseDouble(row.get(1)) >= 7.0 ? Highlight.POSITIVE : Highlight.NEGATIVE;
    } catch (NumberFormatException e) {
      return Highlight.STRONG;
    }
  }
}
