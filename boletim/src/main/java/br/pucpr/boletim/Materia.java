package br.pucpr.boletim;

import java.util.List;

/** Uma linha do notas.csv. */
public record Materia(String nome, String professor, List<Double> notas) {

  public double media() {
    double soma = 0;
    for (double n : notas) {
      soma += n;
    }
    return notas.isEmpty() ? 0 : soma / notas.size();
  }

  public boolean aprovado() {
    return media() >= 7.0;
  }
}
