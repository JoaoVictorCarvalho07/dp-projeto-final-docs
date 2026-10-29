package br.pucpr.docs.export;

import java.util.List;

/**
 * Strategy: o cliente diz quais linhas de tabela merecem destaque (ex: a média do aluno). A lib
 * não sabe nada de notas, só sabe pintar o destaque com as cores do tema.
 */
@FunctionalInterface
public interface RowHighlighter {
  enum Highlight {
    NONE,
    STRONG,
    POSITIVE,
    NEGATIVE
  }

  Highlight highlight(List<String> headers, List<String> row);

  RowHighlighter NONE = (h, r) -> Highlight.NONE;
}
