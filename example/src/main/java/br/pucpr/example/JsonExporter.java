package br.pucpr.example;

import br.pucpr.docs.export.AbstractExporter;
import br.pucpr.docs.model.Document;
import br.pucpr.docs.model.Node;
import br.pucpr.docs.model.Paragraph;
import br.pucpr.docs.model.Section;
import br.pucpr.docs.model.Table;

/**
 * Exportador criado FORA da biblioteca (Open/Closed): a lib não foi alterada pra suportar JSON.
 * Só estendemos AbstractExporter (Template Method) e implementamos os visit* (Visitor).
 */
public class JsonExporter extends AbstractExporter {
  private boolean first;

  @Override
  protected void writeHeader(Document d) {
    out.append("{\"title\":").append(q(d.getTitle())).append(",\"author\":").append(q(d.getAuthor()));
    out.append(",\"content\":[");
    first = true;
  }

  @Override
  protected void writeFooter(Document d) {
    out.append("]}");
  }

  @Override
  public void visitSection(Section s) {
    comma();
    out.append("{\"section\":").append(q(s.getTitle())).append(",\"children\":[");
    first = true;
    for (Node child : s.getChildren()) {
      child.accept(this);
    }
    out.append("]}");
    first = false;
  }

  @Override
  protected void writeSectionTitle(Section s, int level) {
    // não usado: visitSection foi sobrescrito
  }

  @Override
  public void visitParagraph(Paragraph p) {
    comma();
    out.append("{\"paragraph\":").append(q(p.getText())).append('}');
  }

  @Override
  public void visitTable(Table t) {
    comma();
    out.append("{\"table\":{\"headers\":").append(arr(t.getHeaders())).append(",\"rows\":[");
    for (int i = 0; i < t.getRows().size(); i++) {
      if (i > 0) {
        out.append(',');
      }
      out.append(arr(t.getRows().get(i)));
    }
    out.append("]}}");
  }

  private void comma() {
    if (!first) {
      out.append(',');
    }
    first = false;
  }

  private static String arr(java.util.List<String> xs) {
    return "[" + String.join(",", xs.stream().map(JsonExporter::q).toList()) + "]";
  }

  private static String q(String s) {
    return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
  }
}
