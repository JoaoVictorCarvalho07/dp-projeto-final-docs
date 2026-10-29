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

Tres classes novas, todas aqui no modulo `boletim`. O `CsvExporter` gera `;` e BOM UTF-8
(abre direto no Excel em portugues), colunas `Materia;Professor;Avaliacao;Nota`:

| Classe | Estende / implementa | Pattern |
| --- | --- | --- |
| `CsvExporter` | `AbstractExporter` | Template Method + Visitor |
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
- **extensoes do boletim:** CSV e DOCX

Cada formato e um card (badge "lib" ou "extensao") com "Visualizar" e "Baixar". O Visualizar abre
no painel de previa embaixo: HTML e PDF num iframe, Markdown e CSV num bloco de texto. DOCX o
navegador nao mostra, entao o painel so oferece o download. O botao no topo troca o tema
(Classico / PUCPR), e tambem da pra usar `?tema=classico` ou `?tema=pucpr` na URL.

As medias aparecem em destaque em todos os formatos (verde se >= 7, vermelho se nao). Isso e o
`DestaqueMedia`, um `RowHighlighter` (Strategy da lib) criado aqui no boletim: a lib nao sabe o
que e nota, so sabe pintar a linha com as cores do tema.

## Temas

| | Classico (lib) | PUCPR (boletim) |
| --- | --- | --- |
| Fonte | serifada (Georgia / Times) | sans-serif (Segoe UI / Arial / Helvetica) |
| Cores | preto e branco | vinho `#7a1a2b` com detalhe dourado `#c9a227` |
| Tabela | cabecalho cinza, grade preta | cabecalho vinho, linhas zebradas |
| Titulos | MAIUSCULAS | normais |

Cada tema devolve uma `Palette` (cores, fontes e tamanhos) no `createPalette()`, e o HTML, o PDF
e o DOCX leem tudo dela, por isso os tres saem com a mesma cara.
O servidor usa o `HttpServer` do proprio JDK, sem framework (`ServidorWeb`).

### Por que o DOCX tem uma interface propria

O `DocumentExporter` da lib devolve `String`, e DOCX e binario (um zip). Em vez de mudar a lib,
criamos aqui no boletim a `BinaryExporter` (devolve `byte[]`) e a `AbstractBinaryExporter`, que
faz o mesmo template method do `AbstractExporter` e implementa o `DocumentVisitor` da lib pra
andar na arvore. O `DocxExporter` estende essa base. Ou seja: a lib foi estendida com um tipo
de saida que ela nem previa, sem mexer numa linha dela (Open/Closed).

O DOCX e montado na mao (`[Content_Types].xml`, `_rels/.rels`, `word/document.xml`,
`word/styles.xml` com os estilos de titulo e `word/footer1.xml` com "Pagina X de N", num zip),
sem Apache POI, pra nao puxar uma dependencia pesada.

Os decorators da lib trabalham com texto, entao no site so o Markdown passa por eles (marca
d'agua RASCUNHO no classico, CONFIDENCIAL no pucpr, e numeracao de paginas). HTML, PDF, DOCX e CSV
nao passam: estragaria o arquivo (no CSV o Excel ia ler a marca d'agua como linha de dados, e no
HTML ia aparecer texto solto fora da pagina). Nas demos de terminal os decorators continuam. No DOCX o aviso (RASCUNHO/CONFIDENCIAL) vai como primeiro paragrafo.

## Lib intacta

Na lib entraram melhorias gerais (decisao do grupo): o `PdfExporter`, a `Palette` dos temas e o
`RowHighlighter`. Nada especifico do boletim foi pra lib:

```bash
git diff main -- lib/
```

So aparece o `PdfExporter` (e o teste dele); o resto continua igual.
