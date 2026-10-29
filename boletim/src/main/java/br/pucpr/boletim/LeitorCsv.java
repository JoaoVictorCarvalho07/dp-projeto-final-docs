package br.pucpr.boletim;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Le o arquivo de notas. Formato: materia,professor,nota1,nota2,... (primeira linha = cabecalho) */
public class LeitorCsv {

  public static List<Materia> ler(Path arquivo) throws IOException {
    return lerLinhas(Files.readAllLines(arquivo));
  }

  public static List<Materia> lerLinhas(List<String> linhas) {
    List<Materia> materias = new ArrayList<>();
    for (int i = 1; i < linhas.size(); i++) {
      String linha = linhas.get(i).trim();
      if (linha.isEmpty()) {
        continue;
      }
      String[] partes = linha.split(",");
      List<Double> notas = new ArrayList<>();
      for (int j = 2; j < partes.length; j++) {
        notas.add(Double.parseDouble(partes[j].trim()));
      }
      materias.add(new Materia(partes[0].trim(), partes[1].trim(), notas));
    }
    return materias;
  }
}
