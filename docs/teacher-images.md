# Imagens da professora — validação dos desafios

Os 15 arquivos originais fornecidos pelo usuário estão em [`src/test/resources/teacher-images`](../src/test/resources/teacher-images). Foram extraídos do ZIP sem conversão, recorte ou redimensionamento. O [manifesto](../src/test/resources/teacher-images/manifest.json) registra SHA-256, tamanho e dimensões de cada arquivo e o SHA-256 do ZIP.

## Resultado após as correções

**15 imagens aprovadas, nenhuma falha, erro de execução ou omissão.** A avaliação usa Linux x86-64 e JDK 21. Os resultados completos e os hashes dos fontes avaliados estão no [relatório JSON](teacher-images-results.json).

Os horários esperados são **07:31**, **03:00 ou 02:59**, **10:15**. O usuário confirmou 10:15 para o terceiro arquivo após a conferência da posição do ponteiro. A indicação anterior de 10:20 foi corrigida no gabarito; não foi introduzida uma regra para converter 15 minutos em 20.

| Arquivo | Expectativa | Saída atual | Resultado |
|---|---|---|---|
| `Formas 1.png` | 3 quadrados vermelhos, 5 círculos verdes e 2 círculos azuis; total 10 | Contagens correspondentes | Aprovado |
| `Formas 2.jpeg` | 4 círculos vermelhos, 1 círculo amarelo e 2 quadrados azuis; total 7 | Contagens correspondentes | Aprovado |
| `Formas 3.jpeg` | 6 quadrados verdes e 3 círculos amarelos; total 9 | Contagens correspondentes | Aprovado |
| `Grafico 1.jpeg` | Maior 20; menor 5 | Maior 20; menor 5 | Aprovado |
| `Grafico 2.jpeg` | Maior 7; menor 7 | Maior 7; menor 7 | Aprovado |
| `Letras 1.jpeg` | `A, B, C` | `A, B, C` | Aprovado |
| `Letras 2.jpeg` | `X, Y, Z` | `X, Y, Z` | Aprovado |
| `Letras 3.jpeg` | `M, M, M` | `M, M, M` | Aprovado |
| `Placa 1.jpeg` | Velocidade máxima | `Velocidade maxima` | Aprovado por categoria |
| `Placa 2.jpeg` | Pare | `Pare` | Aprovado |
| `Placa 3.jpeg` | Sentido proibido / proibido seguir em frente | `Sentido proibido` | Aprovado |
| `Placa 4.jpeg` | Proibido estacionar | `Proibido estacionar` | Aprovado |
| `Relogio 1.jpeg` | `07:31` | `07:31` | Aprovado |
| `Relogio 2.jpeg` | `03:00` ou `02:59` | `02:59` | Aprovado |
| `Relogio 3.jpeg` | `10:15`, confirmado pelo usuário | `10:15` | Aprovado |

Na primeira placa, a imagem indica **50 km/h**, mas o classificador continua informando somente a categoria. A aprovação dessa imagem não demonstra extração do limite numérico.

## Correções realizadas

- **Letras:** os templates passam a usar as famílias lógicas `SansSerif` e `Serif` em negrito e uma variação de espessura de um pixel na máscara normalizada. O limiar de IoU permanece em 0,70. Formas sólidas convexas e largas são rejeitadas antes da classificação; traços estreitos continuam disponíveis para a letra I.
- **Gráficos:** os dígitos passam a ter templates regulares e em negrito nas duas famílias. Isso permite ler números finos da escala sem reduzir o limiar de 0,55. A seleção de barras, a interpolação e o tratamento de valores desconhecidos foram preservados.
- **Relógios:** um segmento só é dividido em dois ponteiros quando passa perto do centro. Os comprimentos são comparados pela distância radial da ponta ao centro, evitando que um segmento de Hough produza um ponteiro curto falso. O primeiro relógio passa de 11:31 para 07:31.
- **Manutenção:** `GlyphTemplates` compartilha geração e classificação de letras/dígitos. O cache contém apenas bytes Java; as matrizes nativas temporárias continuam pertencendo ao `CvScope` da análise. As comparações continuam medindo IoU do primeiro plano.

O processamento não lê nomes de arquivos, hashes, gabaritos ou imagens de teste. Os templates são gerados de fontes locais; não foram adicionadas bibliotecas, modelos treinados ou fontes baixadas.

## Reprodução e regressão

[`TeacherImagesTest`](../src/test/java/src/TeacherImagesTest.java) usa `ImageFiles.read` e os mesmos métodos `evaluate` chamados pela interface. A suíte foi incluída nos testes padrão, inclusive na etapa headless da CI:

```bash
./mvnw clean verify

# Somente as 15 imagens da professora
./mvnw -Dtest=TeacherImagesTest test

# Inclui os testes que abrem janelas
xvfb-run -a ./mvnw -Pdesktop verify
```

No ambiente de nuvem preparado, o helper `/workspace/pdi-environment/mvn-pdi` configura JDK e caches locais.

Os relatórios JUnit ficam em `target/surefire-reports/TEST-src.TeacherImagesTest.xml` e `target/surefire-reports/src.TeacherImagesTest.txt`. Cada caso também imprime uma linha `TEACHER_RESULT` com arquivo, desafio, hash, dimensões, expectativa, status, saída, detalhes e duração. Os hashes da execução são conferidos com os originais no relatório salvo.

Foram acrescentados testes de rejeição para quatro formas geométricas pretas e leituras sintéticas de 07:31, 02:59, 10:15, 10:20, 11:45 e 04:10. Os testes existentes de A–Z, imagens desconhecidas, gráficos sem escala, valor zero, exemplos versionados e recursos nativos foram preservados.

## Comparação e limites

A [execução anterior às correções](teacher-images-baseline.json), no commit `f54fb882f97f20961a35ab6b505a8bf16d98bd01`, registrou oito aprovações e sete divergências com o gabarito original. Uma divergência era o terceiro relógio, cujo gabarito foi corrigido pelo usuário para 10:15; as outras seis foram resolvidas no processamento.

As imagens da professora foram usadas para ajustar e verificar os reconhecedores. São agora exemplos de ajuste/regressão, e não uma avaliação independente de generalização. A aprovação desse conjunto de ilustrações não garante OCR de qualquer fonte, reconhecimento geral de fotografias ou identificação de placas arbitrárias. A resolução das fontes lógicas também depende da plataforma; Windows/macOS ainda precisam de execução própria.
