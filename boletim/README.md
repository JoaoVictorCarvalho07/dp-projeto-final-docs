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

## Versao web (visualizar / baixar)

```bash
./gradlew :boletim:runWeb          # Linux/Mac
.\gradlew.bat :boletim:runWeb      # Windows
```

Abre http://localhost:8080 . A pagina lista os formatos:

- **da lib:** Markdown, HTML e PDF (`PdfExporter`, PDF de verdade escrito na mao dentro da lib)
- **extensoes do boletim:** LaTeX e DOCX

Cada um tem "Visualizar" (abre no navegador) e "Baixar" (download com o nome do arquivo certo).
Da pra trocar o tema com `?tema=classico` ou `?tema=pucpr`. No classico os textos levam
`WatermarkDecorator("RASCUNHO")`, no pucpr levam o `ConfidencialDecorator`, os dois com `PageNumberDecorator`.
O servidor usa o `HttpServer` do proprio JDK, sem framework (`ServidorWeb`).

### Por que o DOCX tem uma interface propria

O `DocumentExporter` da lib devolve `String`, e DOCX e binario (um zip). Em vez de mudar a lib,
criamos aqui no boletim a `BinaryExporter` (devolve `byte[]`) e a `AbstractBinaryExporter`, que
faz o mesmo template method do `AbstractExporter` e implementa o `DocumentVisitor` da lib pra
andar na arvore. O `DocxExporter` estende essa base. Ou seja: a lib foi estendida com um tipo
de saida que ela nem previa, sem mexer numa linha dela (Open/Closed).

O DOCX e montado na mao (`[Content_Types].xml`, `_rels/.rels` e `word/document.xml` num zip),
sem Apache POI, pra nao puxar uma dependencia pesada.

Os decorators da lib trabalham com texto, entao PDF e DOCX nao passam por eles (estragaria o
arquivo). No DOCX o aviso (RASCUNHO/CONFIDENCIAL) vai como primeiro paragrafo.

## Lib intacta

O PDF foi adicionado na propria lib (decisao do grupo). Fora isso, nada do boletim mexe na lib:

```bash
git diff main -- lib/
```

So aparece o `PdfExporter` (e o teste dele); o resto continua igual.
