# Validação e limites da evidência

A suíte contém testes de regressão para alfa, imagens cinza uniformes, rotação de marcadores, escala, morfologia, afinamento, histórico e arquivos. Exercita também recursos nativos, entradas numéricas inválidas e os exemplos versionados.

Os testes de reconhecimento verificam as quatro formas nas quatro cores, letras A–Z renderizadas na mesma família de fonte dos templates, candidatos sintéticos de Pare/velocidade máxima, imagens vazias, entradas desconhecidas, gráficos com e sem rótulos, valor zero e relógios de 03:00/06:00.

A validação desktop carrega imagem por arquivo, aplica grayscale pelo menu, desfaz e reseta. A captura `application.png` foi produzida pela aplicação em execução. Os diálogos nativos de seleção de arquivo e o gesto físico de drag-and-drop não são automatizados por esse teste; o serviço de leitura/gravação é exercitado separadamente.

## Reproduzir

```bash
./mvnw clean verify
xvfb-run -a ./mvnw -Pdesktop verify
```

Leia os relatórios de cada execução em `target/surefire-reports/`. O perfil headless omite somente o teste da janela. Uma execução desktop deve completá-lo, sem testes omitidos. Não há medição de cobertura nem benchmark de latência.

## O que esses testes não demonstram

A renderização usada para A–Z é próxima da geração de templates; os resultados demonstram integração e regressão, não generalização de OCR. Os limiares e formatos foram verificados em exemplos controlados. Não houve avaliação com fotografias, ruído real, múltiplas fontes independentes, sinais de trânsito variados ou gráficos de layouts arbitrários.

Uma futura avaliação deve separar exemplos de ajuste e teste; registrar rótulos reais, status inconclusivo, falsos positivos e negativos por módulo; variar resolução e iluminação; e medir tempo e memória em condições descritas. Só então será adequado publicar métricas de qualidade ou desempenho.
