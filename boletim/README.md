# Mini projeto: gerador de boletim

Usa a lib de documentos (`lib/`) pra gerar o boletim do semestre a partir de um CSV de notas.

## Como rodar

Na raiz do repositorio:

```bash
./gradlew :boletim:run           # demo padrao (so a lib)
./gradlew :boletim:runExtensao   # demo com extensoes
./gradlew :boletim:test          # testes
```

O arquivo lido e o `boletim/notas.csv` (`materia,professor,nota1,nota2,...`).

## Caso padrao (`DemoPadrao`)

Monta o documento com o `DocumentBuilder` (uma secao de resumo e uma secao por materia, com tabela de notas)
e exporta com `MarkdownExporter` e `HtmlExporter` usando o `ClassicThemeFactory`,
embrulhados em `WatermarkDecorator("RASCUNHO")` e `PageNumberDecorator`. Nenhuma classe nova.

## Caso de extensao (`DemoExtensao`)

Tres classes novas, todas aqui no modulo `boletim`:

| Classe | Estende / implementa | Pattern |
| --- | --- | --- |
| `LatexExporter` | `AbstractExporter` | Template Method + Visitor |
| `PucprThemeFactory` | `ThemeFactory` | Abstract Factory |
| `ConfidencialDecorator` | `ExporterDecorator` | Decorator |

Elas se misturam com o que ja existe na lib (ex.: `HtmlExporter` com o tema PUCPR,
`PageNumberDecorator` por cima do `ConfidencialDecorator`).

Pra provar que a lib nao foi alterada (Open/Closed):

```bash
git diff main -- lib/
```

A saida tem que ser vazia.
