## Problema e comportamento resultante

A versão completa de PDI continha fontes duplicados, dependência OpenCV sem build reproduzível e defeitos que afetavam pixels, histórico e reconhecimento. A aplicação agora compila pela raiz com Maven Wrapper, carrega o OpenCV correspondente sob demanda e preserva a interface Swing e os cinco exercícios.

As correções incluem alfa e tons de cinza, rotação de 90°, escala mínima, histórico atômico, distinção entre leitura ausente e zero em gráficos, rejeição de glifos/candidatos de placas desconhecidos e separação de ponteiros opostos. Processamento e arquivos usam um worker com ações concorrentes bloqueadas. Estado, arquivos e recursos nativos têm responsabilidades separadas e limites explícitos.

## Validação

- Build limpo com JDK 21: 70 testes passaram; um teste de janela foi omitido pelo perfil headless.
- Perfil desktop sob Xvfb: 71 testes passaram, sem falhas ou omissões.
- Executável empacotado: janela principal e carregamento de imagem verificados.
- Exemplos sintéticos versionados e resultados documentados, incluindo A–Z na fonte dos templates, formas nas quatro cores, gráficos e relógios.
- `git diff --check` sem erros.

## Documentação e limites

README com instalação, execução, arquitetura, demonstração e testes; captura real da interface; CI configurada para Java 17/21 e teste desktop em Linux. A execução remota da CI deve ser acompanhada no GitHub Actions.

Reconhecedores continuam heurísticos. Não foram publicados números de precisão ou desempenho em imagens reais. Há estados inconclusivos explícitos, mas não garantia de rejeitar toda entrada fora do domínio. Não foram adicionados LLMs, APIs, automação em lote ou serviços pagos.
