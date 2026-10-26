package br.pucpr.docs.iterator;

import br.pucpr.docs.model.Node;
import br.pucpr.docs.model.Section;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/** ITERATOR: percorre a árvore em profundidade (pré-ordem) sem expor a estrutura interna. */
public final class DepthFirstIterator implements Iterator<Node> {
  private final Deque<Node> stack = new ArrayDeque<>();

  public DepthFirstIterator(Node root) {
    stack.push(root);
  }

  @Override
  public boolean hasNext() {
    return !stack.isEmpty();
  }

  @Override
  public Node next() {
    if (stack.isEmpty()) {
      throw new NoSuchElementException();
    }
    Node current = stack.pop();
    if (current instanceof Section section) {
      List<Node> children = section.getChildren();
      // empilha de trás pra frente pra visitar na ordem original
      for (int i = children.size() - 1; i >= 0; i--) {
        stack.push(children.get(i));
      }
    }
    return current;
  }
}
