package br.pucpr.docs.visitor;

import br.pucpr.docs.model.Paragraph;
import br.pucpr.docs.model.Section;
import br.pucpr.docs.model.Table;

/**
 * VISITOR: uma operação nova sobre a árvore (ex: um exportador novo) é só uma classe nova que
 * implementa esta interface. As classes do modelo não mudam (Open/Closed).
 */
public interface DocumentVisitor {
  void visitSection(Section section);

  void visitParagraph(Paragraph paragraph);

  void visitTable(Table table);
}
