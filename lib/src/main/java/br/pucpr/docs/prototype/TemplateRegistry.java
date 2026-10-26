package br.pucpr.docs.prototype;

import br.pucpr.docs.model.Document;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * PROTOTYPE: guarda documentos-modelo e devolve sempre um CLONE profundo. Quem usa pode editar a
 * cópia sem estragar o modelo original.
 */
public final class TemplateRegistry {
  private final Map<String, Document> templates = new HashMap<>();

  public void register(String name, Document prototype) {
    templates.put(name, prototype.copy());
  }

  public Document create(String name) {
    Document prototype = templates.get(name);
    if (prototype == null) {
      throw new IllegalArgumentException("Template não encontrado: " + name);
    }
    return prototype.copy();
  }

  public Set<String> names() {
    return Set.copyOf(templates.keySet());
  }
}
