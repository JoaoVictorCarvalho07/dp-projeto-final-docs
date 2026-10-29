package br.pucpr.boletim;

import br.pucpr.docs.builder.DocumentBuilder;
import br.pucpr.docs.model.Document;
import java.util.List;
import java.util.Locale;

/** Monta o Document do boletim usando o DocumentBuilder da lib. */
public class GeradorBoletim {

  public static Document gerar(String aluno, List<Materia> materias) {
    DocumentBuilder b = DocumentBuilder.create("Boletim do Semestre").author(aluno);

    int aprovadas = 0;
    double somaMedias = 0;
    for (Materia m : materias) {
      somaMedias += m.media();
      if (m.aprovado()) {
        aprovadas++;
      }
    }
    double mediaGeral = materias.isEmpty() ? 0 : somaMedias / materias.size();

    b.section("Resumo")
        .paragraph(
            "Materias cursadas: " + materias.size() + ". Aprovado em " + aprovadas
                + ". Media geral: " + fmt(mediaGeral) + ".")
        .endSection();

    for (Materia m : materias) {
      b.section(m.nome())
          .paragraph("Professor(a): " + m.professor() + " - Situacao: "
              + (m.aprovado() ? "Aprovado" : "Reprovado"));
      b.table("Avaliacao", "Nota");
      for (int i = 0; i < m.notas().size(); i++) {
        b.row("N" + (i + 1), fmt(m.notas().get(i)));
      }
      b.row("Media", fmt(m.media()));
      b.endSection();
    }
    return b.build();
  }

  static String fmt(double v) {
    return String.format(Locale.US, "%.1f", v);
  }
}
