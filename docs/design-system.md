# Design system — PDI

## Direção e diagnóstico

O redesign mantém a aplicação Java Swing e os algoritmos existentes. A referência é a organização discreta de ferramentas e conteúdo: superfícies claras, hierarquia curta, tipografia legível e um único acento. Não há migração para web, bibliotecas visuais externas, fontes remotas, gradientes ou sombras decorativas.

A inspeção inicial encontrou seis menus com nomes longos, estilos repetidos em cada painel, formulários com campos em linha, foco visual removido de alguns botões, ausência de orientação na área vazia e diálogos intermediários para operações sem parâmetros. A solução usa um catálogo compartilhado de ações, uma lateral por categoria e feedback dentro da área de trabalho.

## Tokens

Os valores são definidos em `src/main/java/src/ui/Theme.java`.

| Papel | Valor |
|---|---|
| Superfície principal | `#FFFFFF` |
| Lateral | `#F7F7F5` |
| Área de imagem | `#F4F4F1` |
| Texto principal | `#292925` |
| Texto secundário | `#666660` |
| Separador | `#E3E3DE` |
| Limite de input | `#8B8B83` |
| Acento | `#315C70` |
| Seleção suave | `#E6EEF1` |
| Tipografia | SansSerif do sistema; 12, 14 e 22 px; regular e negrito |
| Espaçamento | 4, 8, 12, 16, 24 e 32 px |
| Raio | 8 px |
| Altura mínima de controles | 36 px; ferramentas compactas: 32 px |
| Lateral expandida / recolhida | 248 / 64 px |
| Largura preferencial de diálogo | 480 px, limitada à área útil do display |

As cores das imagens fazem parte do conteúdo e não são restringidas à paleta da interface. As fontes não são baixadas: a família lógica do Java usa uma alternativa local, validada aqui com fontes do ambiente Linux.

## Componentes e estados

- **AppButton:** ação primária preenchida, secundária com contorno, ações auxiliares discretas e navegação selecionada. Hover usa superfície sutil; pressionado escurece; foco de teclado desenha um contorno contrastante de 2 px (branco na ação primária e azul nos demais); desabilitado mantém o rótulo legível e não executa a ação.
- **LineIcon:** ícones de traço simples que herdam a cor e o estado do controle. Botões sem texto têm nome acessível e tooltip.
- **ToolSidebar:** cinco categorias e uma lista rolável. Seleção muda a categoria visível; no modo recolhido, cada ícone abre as ferramentas em um menu acessível por teclado.
- **ParameterForm:** labels acima dos controles, hints com unidades, uma ação principal, Cancelar e mensagem de validação no próprio formulário. A região de campos pode rolar; as ações ficam fora dela.
- **Dialogs:** título curto e conteúdo proporcional à tarefa. Enter aciona a ação principal; Esc, Cancelar ou o fechamento da janela descartam somente o formulário. A substituição de arquivo exige confirmação explícita. Mensagens informativas têm apenas Fechar.
- **ImageCanvas:** comparação com ajuste à área ou tamanho real com rolagem. Alterar a visualização não modifica a imagem editada.
- **Feedback inline:** título, estado por texto e conteúdo selecionável. Resultados identificados, inconclusivos, ausentes e erros não dependem apenas de cor. Após uma análise, o texto recebe foco; Esc fecha o painel e devolve o foco ao editor.

As mesmas `Action` controlam menus, lateral, botões e atalhos. Isso mantém nomes, callbacks e disponibilidade sincronizados. Durante processamento, ações que alteram o documento ficam desabilitadas; o progresso é indeterminado, sem percentuais inventados.

## Layout adaptativo

- Abaixo de 1020 px de largura, a lateral recolhe automaticamente. O usuário pode sobrescrever essa escolha pelo botão ou atalho.
- A comparação empilha quando a área útil tem menos de 760 px e a janela tem pelo menos 680 px de altura. Em janelas baixas, mantém lado a lado para preservar espaço vertical para as imagens.
- Sliders, campos e textos respeitam o espaço disponível. Diálogos são limitados à área útil do monitor e podem rolar os campos sem esconder as ações.
- Seletores de arquivo permanecem nativos do sistema operacional, preservando seus recursos de navegação e acessibilidade.

## Acessibilidade e validação

Foram exercitados Tab, Enter, Esc, foco visível, atalhos, associação de labels, nomes acessíveis e o recebimento de arquivos por transferência. `ThemeTest` verifica contraste mínimo de 4,5:1 para texto principal/secundário e ação primária, e 3:1 para foco e limites de inputs.

Os testes cobrem janelas de 760×520, 800×600, 800×900 e 1440×900; os diálogos também são executados em um display virtual de 800×600. As capturas são feitas por `Robot` na aplicação real. Não houve auditoria com leitores de tela ou validação presencial em Windows/macOS; não se declara conformidade completa com um padrão de acessibilidade.

## Capturas reais

- [Interface anterior](application-before.png)
- [Comparação após o redesign](application.png)
- [Área vazia](ui-empty.png)
- [Janela compacta](ui-compact.png)
- [Validação inline](ui-form-validation.png)
- [Resultado de análise](ui-analysis.png)
