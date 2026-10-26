package br.pucpr.docs.model;

import br.pucpr.docs.iterator.DepthFirstIterator;
import java.util.Iterator;

/**
 * Raiz do documento. ITERATOR: é Iterable, então dá pra usar for-each nos nós. PROTOTYPE: copy()
 * gera um documento novo e independente.
 */
public final class Document implements Iterable<Node> {
  private String title;
  private String author;
  private final Section root;

  public Document(String title, String author, Section root) {
    this.title = title;
    this.author = author;
    this.root = root;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public Section getRoot() {
    return root;
  }

  @Override
  public Iterator<Node> iterator() {
    return new DepthFirstIterator(root);
  }

  public Document copy() {
    return new Document(title, author, root.copy());
  }
}
