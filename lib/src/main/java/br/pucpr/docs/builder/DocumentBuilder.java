package br.pucpr.docs.builder;

import br.pucpr.docs.model.Document;
import br.pucpr.docs.model.Paragraph;
import br.pucpr.docs.model.Section;
import br.pucpr.docs.model.Table;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * BUILDER: monta o documento passo a passo com uma API fluente, escondendo a montagem da árvore.
 *
 * <pre>
 * Document d = DocumentBuilder.create("Relatório")
 *     .author("Grupo")
 *     .section("Intro").paragraph("Olá").endSection()
 *     .build();
 * </pre>
 */
public final class DocumentBuilder {
  private final String title;
  private String author = "";
  private final Section root = new Section("root");
  private final Deque<Section> open = new ArrayDeque<>();
  private Table currentTable;

  private DocumentBuilder(String title) {
    this.title = title;
    open.push(root);
  }

  public static DocumentBuilder create(String title) {
    return new DocumentBuilder(title);
  }

  public DocumentBuilder author(String author) {
    this.author = author;
    return this;
  }

  /** Abre uma seção (pode ser aninhada). Feche com {@link #endSection()}. */
  public DocumentBuilder section(String title) {
    Section section = new Section(title);
    open.peek().add(section);
    open.push(section);
    currentTable = null;
    return this;
  }

  public DocumentBuilder endSection() {
    if (open.size() == 1) {
      throw new IllegalStateException("Nenhuma seção aberta");
    }
    open.pop();
    currentTable = null;
    return this;
  }

  public DocumentBuilder paragraph(String text) {
    open.peek().add(new Paragraph(text));
    currentTable = null;
    return this;
  }

  public DocumentBuilder table(String... headers) {
    currentTable = new Table(Arrays.asList(headers));
    open.peek().add(currentTable);
    return this;
  }

  public DocumentBuilder row(String... cells) {
    if (currentTable == null) {
      throw new IllegalStateException("Chame table(...) antes de row(...)");
    }
    currentTable.addRow(Arrays.asList(cells));
    return this;
  }

  public Document build() {
    if (title == null || title.isBlank()) {
      throw new IllegalStateException("Documento precisa de título");
    }
    return new Document(title, author, root);
  }
}
