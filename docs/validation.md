# Validação e limites da evidência

## Portabilidade da leitura de dígitos na CI

Os logs da execução do commit `22df7da` confirmaram uma falha em `ExamplesTest`: `examples/barras.png` deveria produzir `Maior = 20 | Menor = 10`, mas retornava `Maior = 20 | Menor = 40`, com Java 17 e 21. As 15 imagens da professora passavam nessa mesma execução.

A leitura incorreta foi reproduzida ao restringir as fontes locais com Fontconfig. Referências binárias de dígitos geradas de fontes, independentes das imagens de teste, agora complementam os templates locais. O teste conserva o resultado esperado 10; o limiar de similaridade continua em 0,55.

A CI também executa `ExamplesTest` com [uma configuração alternativa de fontes](../.github/fontconfig-dejavu.conf). Para reproduzir em Linux com DejaVu instalado:

```bash
FONTCONFIG_FILE="$PWD/.github/fontconfig-dejavu.conf" ./mvnw -Dtest=ExamplesTest test
```

A suíte contém testes de regressão para alfa, imagens cinza uniformes, rotação de marcadores, escala, morfologia, afinamento, histórico e arquivos. Exercita também recursos nativos, entradas numéricas inválidas e os exemplos versionados.

Os testes de reconhecimento verificam as quatro formas nas quatro cores, letras A–Z renderizadas na mesma família de fonte dos templates, candidatos sintéticos de Pare/velocidade máxima, imagens vazias, entradas desconhecidas, gráficos com e sem rótulos, valor zero e relógios de 03:00/06:00.

A validação desktop carrega imagem por arquivo, aplica grayscale pelo menu, desfaz e reseta. Também exercita resultado inline, erro de leitura preservando a imagem anterior, Enter/Esc em parâmetros, popup da lateral recolhida, tamanho real, importação por `TransferHandler` e janelas de 760×520 a 1440×900. Os diálogos de parâmetros são verificados também em um display virtual de 800×600. As capturas em `docs/` foram produzidas pela aplicação em execução.

Os diálogos nativos de seleção de arquivo e o gesto físico de drag-and-drop não são automatizados por esses testes; o serviço de leitura/gravação e o recebimento da transferência são exercitados separadamente. Os testes de formulários verificam valores, limites, callbacks e labels associados; o teste de tema calcula contraste para texto, foco e limites de inputs. Isso não equivale a uma auditoria completa com leitor de tela.

## Reproduzir

```bash
./mvnw clean verify
xvfb-run -a ./mvnw -Pdesktop verify
```

Leia os relatórios de cada execução em `target/surefire-reports/`. O perfil headless omite os quatro testes que exigem display. Uma execução desktop deve completá-los, sem testes omitidos. Não há medição de cobertura nem benchmark de latência.

## O que esses testes não demonstram

As 15 imagens originais fornecidas pela professora são verificadas por `TeacherImagesTest`, incluído nos testes padrão e no perfil desktop: **15 aprovações**, sem falhas, erros ou omissões. Os horários são os confirmados pelo usuário, incluindo 10:15 no terceiro relógio. O [relatório do conjunto](teacher-images.md) registra cada saída, as correções, a comparação com a avaliação anterior e como reproduzir a execução.

A renderização usada para A–Z é próxima da geração de templates; os resultados desses testes sintéticos demonstram integração e regressão, não generalização de OCR. A avaliação externa acima cobre apenas as 15 ilustrações fornecidas. Isso não demonstra generalização para fotografias, ruído real, outras famílias tipográficas, variações de placas ou gráficos de layouts arbitrários.

Uma futura avaliação deve separar exemplos de ajuste e teste; registrar rótulos reais, status inconclusivo, falsos positivos e negativos por módulo; variar resolução e iluminação; e medir tempo e memória em condições descritas. Só então será adequado publicar métricas de qualidade ou desempenho.


## Verificação da entrega do redesign

- Build com `clean verify` e perfil desktop: **79 testes aprovados**, sem falhas, erros ou testes omitidos.
- Perfil headless: **75 testes aprovados**, quatro testes de desktop omitidos, sem falhas ou erros.
- Diálogos em display virtual de 800×600: teste aprovado, incluindo validação e acesso aos botões de ação.
- Formatação dos 37 arquivos Java alterados ou adicionados verificada; `git diff --check` sem erros.
- Nenhuma dependência nova; processamento, sessão e persistência mantidos.

Verificações executadas em Linux com JDK 21. Leitores de tela, Windows/macOS e os seletores nativos de arquivos não foram automatizados nesta entrega.


## Verificação das correções dos desafios

- `clean verify` headless: 104 casos, **100 aprovados**, quatro testes de janela omitidos, nenhuma falha ou erro.
- `verify` com perfil desktop e Xvfb: **104 aprovados**, nenhuma falha, erro ou omissão.
- As 15 imagens da professora estão incluídas automaticamente em ambos os perfis.
- O terceiro relógio usa 10:15, confirmado pelo usuário após conferir a imagem.
- Os hashes das 15 imagens continuam iguais aos originais do ZIP. A formatação dos seis fontes alterados/adicionados e `git diff --check` foram verificados.

Os [resultados por imagem](teacher-images-results.json) incluem o commit de base e os hashes dos fontes locais avaliados. Os [resultados anteriores](teacher-images-baseline.json) foram preservados para comparação.
