package br.pucpr.docs;

import br.pucpr.docs.builder.DocumentBuilder;
import br.pucpr.docs.model.Document;

final class Fixtures {
  private Fixtures() {}

  static Document sample() {
    return DocumentBuilder.create("Relatório")
        .author("Grupo")
        .section("Intro")
        .paragraph("Olá")
        .section("Sub")
        .paragraph("Detalhe")
        .endSection()
        .endSection()
        .section("Dados")
        .table("Nome", "Nota")
        .row("Ana", "10")
        .endSection()
        .build();
  }
}
