package br.pucpr.docs.model;

import br.pucpr.docs.visitor.DocumentVisitor;

/**
 * COMPOSITE: componente comum da árvore do documento. VISITOR: todo nó aceita um visitante.
 * PROTOTYPE: todo nó sabe se clonar (cópia profunda).
 */
public interface Node {
  void accept(DocumentVisitor visitor);

  Node copy();
}
