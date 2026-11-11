package src;

import src.afinamento.*;
import src.core.*;
import src.desafio.*;
import src.filtros.*;
import src.infra.ImageFiles;
import src.morfologia.*;
import src.transformacoes.*;
import src.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** The desktop workspace coordinates presentation and the existing image services. */
public class Painel extends JFrame {
    private static final Logger LOG = Logger.getLogger(Painel.class.getName());
    private final EditorSession session = new EditorSession();
    private final ImageFiles imageFiles = new ImageFiles();
    private final Map<String, Action> actions = new LinkedHashMap<>();
    private final Set<Action> imageActions = new HashSet<>();
    private final List<ToolSidebar.Group> groups = new ArrayList<>();
    private boolean busy;
    private boolean fit = true;
    private Boolean compactOverride;
    private String arquivoAberto = "";

    private final ImageCanvas original = new ImageCanvas("Imagem original");
    private final ImageCanvas result = new ImageCanvas("Imagem resultante");
    private final JLabel status = Theme.label("Nenhuma imagem carregada", Theme.SMALL, Theme.MUTED);
    private final JLabel document = Theme.label("Nenhuma imagem aberta", Theme.SMALL, Theme.MUTED);
    private final JLabel originalSize = Theme.label("", Theme.SMALL, Theme.MUTED);
    private final JLabel resultSize = Theme.label("", Theme.SMALL, Theme.MUTED);
    private final JProgressBar progress = new JProgressBar();
    private final CardLayout cards = new CardLayout();
    private final JPanel canvasCards = Theme.panel(cards, Theme.PAPER);
    private final JSplitPane comparison = new JSplitPane();
    private final JPanel feedback = Theme.panel(new BorderLayout(Theme.LG, Theme.SM), Theme.PAPER);
    private final JLabel feedbackTitle = Theme.label("", Theme.SECTION, Theme.TEXT);
    private final JLabel feedbackState = Theme.label("", Theme.SMALL, Theme.MUTED);
    private final JTextArea feedbackText = Theme.text("");
    private ToolSidebar sidebar;
    private AppButton fitButton;
    private AppButton openButton;

    private final Grayscale grayscale = new Grayscale();
    private final Limiarizacao limiarizacao = new Limiarizacao();
    private final FiltrodaMediana mediana = new FiltrodaMediana();
    private final ZhangSuen thinning = new ZhangSuen();
    private final LeitorRelogio clock = new LeitorRelogio(grayscale, limiarizacao, thinning);
    private final ContadorObjetos objects = new ContadorObjetos(mediana);
    private final ReconhecedorLetras letters =
            new ReconhecedorLetras(grayscale, limiarizacao, mediana);
    private final IdentificadorPlacas signs = new IdentificadorPlacas();
    private final AnalisadorBarras bars = new AnalisadorBarras(grayscale, limiarizacao);

    public Painel() {
        super("PDI · Processamento de imagens");
        Theme.install();
        setName("pdi.workspace");
        setSize(1240, 820);
        setMinimumSize(new Dimension(760, 520));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        registerActions();
        setJMenuBar(createMenus());
        sidebar =
                new ToolSidebar(
                        groups,
                        () -> {
                            compactOverride = !sidebar.isCompact();
                            adaptLayout();
                        });
        add(sidebar, BorderLayout.WEST);
        JPanel workspace = Theme.panel(new BorderLayout(0, Theme.LG), Theme.PAPER);
        workspace.setBorder(new EmptyBorder(Theme.XL, Theme.XL, Theme.LG, Theme.XL));
        workspace.add(createHeader(), BorderLayout.NORTH);
        JPanel body = Theme.panel(new BorderLayout(0, Theme.LG), Theme.PAPER);
        createCanvas();
        body.add(canvasCards, BorderLayout.CENTER);
        createFeedback();
        body.add(feedback, BorderLayout.SOUTH);
        workspace.add(body, BorderLayout.CENTER);
        workspace.add(createStatus(), BorderLayout.SOUTH);
        add(workspace, BorderLayout.CENTER);
        configureDrop();
        installShortcuts();
        updateState();
        addComponentListener(
                new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        adaptLayout();
                    }
                });
        adaptLayout();
        setLocationRelativeTo(null);
    }

    private Action action(
            String id, String title, String description, boolean needsImage, Runnable callback) {
        Action action =
                new AbstractAction(title) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (isEnabled()) callback.run();
                    }
                };
        action.putValue(Action.ACTION_COMMAND_KEY, id);
        action.putValue(Action.SHORT_DESCRIPTION, description);
        actions.put(id, action);
        if (needsImage) imageActions.add(action);
        return action;
    }

    private Action transform(
            String id, String title, String description, UnaryOperator<BufferedImage> operation) {
        return action(id, title, description, true, () -> applyTransform(operation));
    }

    private Action parameters(String id, String title, String description, Supplier<JPanel> form) {
        return action(
                id,
                title,
                description,
                true,
                () -> Dialogs.parameters(this, title, form.get()).setVisible(true));
    }

    private Action analysis(
            String id,
            String title,
            String description,
            Function<BufferedImage, AnalysisResult> operation) {
        return action(id, title, description, true, () -> runAnalysis(title, operation));
    }

    private void registerActions() {
        action(
                "open",
                "Abrir imagem",
                "Abrir um arquivo de imagem · " + Theme.shortcutHint("O"),
                false,
                this::openImage);
        action(
                "save",
                "Exportar PNG",
                "Salvar a imagem atual em PNG · " + Theme.shortcutHint("S"),
                true,
                this::saveImage);
        action(
                "undo",
                "Desfazer",
                "Desfazer a última operação · " + Theme.shortcutHint("Z"),
                true,
                this::undo);
        action(
                "reset",
                "Resetar",
                "Voltar à imagem original · " + Theme.shortcutHint("Shift+R"),
                true,
                this::reset);
        action(
                "about",
                "Sobre o PDI",
                "Informações sobre o projeto · F1",
                false,
                () ->
                        Dialogs.message(
                                this,
                                "Sobre o PDI",
                                "Processamento Digital de Imagens\n"
                                        + "Fernando Hubner Silva · versão 1.7\n\n"
                                        + "Um espaço para experimentar transformações, filtros e"
                                        + " análise de imagens."));
        action("exit", "Sair", "Encerrar o aplicativo", false, () -> System.exit(0));
        groups.add(
                new ToolSidebar.Group(
                        "Transformar",
                        LineIcon.Kind.TRANSFORM,
                        List.of(
                                parameters(
                                        "rotate",
                                        "Rotacionar",
                                        "Girar a imagem em qualquer direção",
                                        () ->
                                                new RotacionarView(
                                                        p ->
                                                                applyTransform(
                                                                        i ->
                                                                                new Rotacionar()
                                                                                        .rotacionarImagem(
                                                                                                i,
                                                                                                p[
                                                                                                        0],
                                                                                                p[1]
                                                                                                        == 1)))),
                                parameters(
                                        "translate",
                                        "Transladar",
                                        "Deslocar a imagem na área atual",
                                        () ->
                                                new TransladarView(
                                                        p ->
                                                                applyTransform(
                                                                        i ->
                                                                                new Transladar()
                                                                                        .transladarImagem(
                                                                                                i,
                                                                                                p[
                                                                                                        0],
                                                                                                p[
                                                                                                        1])))),
                                parameters(
                                        "mirror",
                                        "Espelhar",
                                        "Inverter os eixos da imagem",
                                        () ->
                                                new EspelhamentoView(
                                                        p ->
                                                                applyTransform(
                                                                        i ->
                                                                                new Espelhamento()
                                                                                        .espelharImagem(
                                                                                                i,
                                                                                                p[
                                                                                                        0],
                                                                                                p[
                                                                                                        1])))),
                                parameters(
                                        "scale",
                                        "Escala",
                                        "Aumentar ou reduzir a imagem",
                                        () ->
                                                new EscalaView(
                                                        f ->
                                                                applyTransform(
                                                                        i ->
                                                                                new Escala()
                                                                                        .aplicarEscala(
                                                                                                i,
                                                                                                f)))))));
        groups.add(
                new ToolSidebar.Group(
                        "Filtros",
                        LineIcon.Kind.FILTER,
                        List.of(
                                transform(
                                        "grayscale",
                                        "Escala de cinza",
                                        "Converter cores em tons de cinza",
                                        grayscale::aplicarGrayscale),
                                transform(
                                        "median",
                                        "Mediana",
                                        "Reduzir ruídos locais",
                                        mediana::aplicarFiltroMediana),
                                transform(
                                        "gaussian",
                                        "Gaussiano",
                                        "Suavizar a imagem",
                                        i -> new FiltrodeGaus().aplicarFiltro(i)),
                                transform(
                                        "sobel",
                                        "Sobel",
                                        "Detectar contornos",
                                        i -> new BordasdeSobel().aplicarFiltro(i)),
                                transform(
                                        "robinson",
                                        "Robinson",
                                        "Detectar contornos direcionais",
                                        i -> new Robinson().aplicarFiltro(i)),
                                parameters(
                                        "log",
                                        "Marr–Hildreth",
                                        "Detectar contornos com suavização",
                                        () ->
                                                new Marrhildrethview(
                                                        (s, t) ->
                                                                applyTransform(
                                                                        i ->
                                                                                new Marrhildreth(
                                                                                                s,
                                                                                                t)
                                                                                        .aplicarFiltro(
                                                                                                i)))),
                                parameters(
                                        "brightness",
                                        "Brilho",
                                        "Clarear ou escurecer",
                                        () ->
                                                new BrilhoView(
                                                        v ->
                                                                applyTransform(
                                                                        i ->
                                                                                new Brilho()
                                                                                        .aplicarBrilho(
                                                                                                i,
                                                                                                v)))),
                                parameters(
                                        "contrast",
                                        "Contraste",
                                        "Ajustar áreas claras e escuras",
                                        () ->
                                                new ContrasteView(
                                                        v ->
                                                                applyTransform(
                                                                        i ->
                                                                                new Contraste()
                                                                                        .aplicarContraste(
                                                                                                i,
                                                                                                v)))),
                                parameters(
                                        "threshold",
                                        "Limiarização",
                                        "Separar áreas claras e escuras",
                                        () ->
                                                new LimiarizacaoView(
                                                        t ->
                                                                applyTransform(
                                                                        i ->
                                                                                limiarizacao
                                                                                        .aplicarLimiarizacao(
                                                                                                i,
                                                                                                t)))))));
        groups.add(
                new ToolSidebar.Group(
                        "Morfologia",
                        LineIcon.Kind.MORPHOLOGY,
                        List.of(
                                transform(
                                        "dilate",
                                        "Dilatação",
                                        "Expandir regiões claras",
                                        i -> new Dilatacao().aplicarFiltro(i)),
                                transform(
                                        "erode",
                                        "Erosão",
                                        "Reduzir regiões claras",
                                        i -> new Erosao().aplicarFiltro(i)),
                                transform(
                                        "opening",
                                        "Abertura",
                                        "Erosão seguida de dilatação",
                                        i -> new Abertura().aplicarFiltro(i)),
                                transform(
                                        "closing",
                                        "Fechamento",
                                        "Dilatação seguida de erosão",
                                        i -> new Fechamento().aplicarFiltro(i)))));
        groups.add(
                new ToolSidebar.Group(
                        "Afinamento",
                        LineIcon.Kind.THINNING,
                        List.of(
                                transform(
                                        "thinning",
                                        "Zhang–Suen",
                                        "Reduzir regiões escuras a linhas finas",
                                        thinning::aplicarThinning))));
        groups.add(
                new ToolSidebar.Group(
                        "Analisar",
                        LineIcon.Kind.ANALYZE,
                        List.of(
                                analysis(
                                        "clock",
                                        "Ler relógio",
                                        "Horário de um relógio analógico",
                                        clock::evaluate),
                                analysis(
                                        "objects",
                                        "Contar objetos",
                                        "Formas e cores na imagem",
                                        objects::evaluate),
                                analysis(
                                        "letters",
                                        "Reconhecer letras",
                                        "Letras maiúsculas de A a Z",
                                        letters::evaluate),
                                analysis(
                                        "signs",
                                        "Identificar placas",
                                        "Candidatos a placas de trânsito",
                                        signs::evaluate),
                                analysis(
                                        "bars",
                                        "Analisar barras",
                                        "Maior e menor valor do gráfico",
                                        bars::evaluate))));
    }

    private JMenuBar createMenus() {
        JMenuBar menu = new JMenuBar();
        menu.setBackground(Theme.SIDEBAR);
        menu.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));
        JMenu file = new JMenu("Arquivo");
        file.setMnemonic(KeyEvent.VK_A);
        file.add(new JMenuItem(actions.get("open")));
        file.add(new JMenuItem(actions.get("save")));
        file.addSeparator();
        file.add(new JMenuItem(actions.get("about")));
        file.add(new JMenuItem(actions.get("exit")));
        menu.add(file);
        int[] mnemonics = {
            KeyEvent.VK_T, KeyEvent.VK_F, KeyEvent.VK_M, KeyEvent.VK_I, KeyEvent.VK_N
        };
        for (int i = 0; i < groups.size(); i++) {
            ToolSidebar.Group group = groups.get(i);
            JMenu category = new JMenu(group.name());
            category.setMnemonic(mnemonics[i]);
            for (Action action : group.tools()) category.add(new JMenuItem(action));
            menu.add(category);
        }
        return menu;
    }

    private AppButton button(String id, LineIcon.Kind icon, AppButton.Style style) {
        AppButton button =
                new AppButton((String) actions.get(id).getValue(Action.NAME), icon, style);
        button.setAction(actions.get(id));
        if (icon != null) button.setIcon(new LineIcon(icon));
        button.setName("action." + id);
        return button;
    }

    private JPanel createHeader() {
        JPanel header = Theme.panel(new BorderLayout(), Theme.PAPER);
        JPanel title = Theme.panel(new BorderLayout(0, Theme.XS), Theme.PAPER);
        title.add(Theme.label("Editor de imagem", Theme.TITLE, Theme.TEXT), BorderLayout.NORTH);
        document.setName("workspace.document");
        document.putClientProperty("html.disable", true);
        title.add(document, BorderLayout.SOUTH);
        JPanel row = Theme.panel(new BorderLayout(Theme.LG, 0), Theme.PAPER);
        row.add(title, BorderLayout.CENTER);
        JPanel files = Theme.panel(new FlowLayout(FlowLayout.RIGHT, Theme.SM, 0), Theme.PAPER);
        openButton = button("open", LineIcon.Kind.OPEN, AppButton.Style.SECONDARY);
        files.add(openButton);
        files.add(button("save", LineIcon.Kind.EXPORT, AppButton.Style.PRIMARY));
        row.add(files, BorderLayout.EAST);
        row.setBorder(new EmptyBorder(0, 0, Theme.LG, 0));
        header.add(row, BorderLayout.NORTH);
        JPanel tools = Theme.panel(new BorderLayout(), Theme.PAPER);
        JPanel edits = Theme.panel(new FlowLayout(FlowLayout.LEFT, Theme.XS, 0), Theme.PAPER);
        edits.add(button("undo", LineIcon.Kind.UNDO, AppButton.Style.GHOST));
        edits.add(button("reset", LineIcon.Kind.RESET, AppButton.Style.GHOST));
        tools.add(edits, BorderLayout.WEST);
        fitButton = new AppButton("Ajustar à área", LineIcon.Kind.FIT, AppButton.Style.GHOST);
        fitButton.setName("view.fit");
        fitButton.setToolTipText(
                "Alternar entre ajustar à área e tamanho real. "
                        + Theme.shortcutHint("0")
                        + " para ajustar.");
        fitButton.addActionListener(e -> setFit(!fit));
        tools.add(fitButton, BorderLayout.EAST);
        JPanel toolbar = Theme.panel(new BorderLayout(0, Theme.MD), Theme.PAPER);
        toolbar.add(tools, BorderLayout.CENTER);
        progress.setName("workspace.progress");
        progress.setPreferredSize(new Dimension(0, 3));
        progress.setBorderPainted(false);
        progress.setForeground(Theme.ACCENT);
        progress.setBackground(Theme.PAPER);
        progress.getAccessibleContext().setAccessibleName("Progresso da operação");
        toolbar.add(progress, BorderLayout.SOUTH);
        header.add(toolbar, BorderLayout.SOUTH);
        return header;
    }

    private void createCanvas() {
        comparison.setName("workspace.comparison");
        comparison.setLeftComponent(imagePane("Original", original, originalSize));
        comparison.setRightComponent(imagePane("Resultado", result, resultSize));
        Theme.splitter(comparison);
        comparison.setContinuousLayout(true);
        comparison.setResizeWeight(.5);
        comparison.setDividerSize(Theme.XS);
        comparison.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        comparison.setBackground(Theme.BORDER);
        JPanel empty = Theme.panel(new GridBagLayout(), Theme.CANVAS);
        empty.setName("workspace.empty");
        JPanel message = Theme.panel(new GridBagLayout(), Theme.CANVAS);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, Theme.LG, 0);
        JLabel icon = new JLabel(new LineIcon(LineIcon.Kind.IMAGE, 36));
        icon.setForeground(Theme.MUTED);
        message.add(icon, c);
        c.gridy++;
        message.add(Theme.label("Abra uma imagem para começar", Theme.SECTION, Theme.TEXT), c);
        c.gridy++;
        message.add(
                Theme.label(
                        "Compare o original e o resultado em um só lugar.",
                        Theme.BODY,
                        Theme.MUTED),
                c);
        c.gridy++;
        message.add(button("open", LineIcon.Kind.OPEN, AppButton.Style.SECONDARY), c);
        c.gridy++;
        c.insets = new Insets(0, 0, 0, 0);
        message.add(
                Theme.label("ou arraste um arquivo para esta área", Theme.SMALL, Theme.MUTED), c);
        empty.add(message);
        canvasCards.add(empty, "empty");
        canvasCards.add(comparison, "image");
        cards.show(canvasCards, "empty");
    }

    private JPanel imagePane(String title, ImageCanvas canvas, JLabel size) {
        JPanel panel = Theme.panel(new BorderLayout(), Theme.CANVAS);
        panel.setMinimumSize(new Dimension(0, 0));
        JPanel heading = Theme.panel(new BorderLayout(Theme.MD, 0), Theme.PAPER);
        heading.setBorder(new EmptyBorder(Theme.MD, Theme.LG, Theme.MD, Theme.LG));
        heading.add(Theme.label(title, Theme.SECTION, Theme.TEXT), BorderLayout.WEST);
        heading.add(size, BorderLayout.EAST);
        panel.add(heading, BorderLayout.NORTH);
        JScrollPane scroll = Theme.scroll(canvas);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void createFeedback() {
        feedback.setName("workspace.feedback");
        feedback.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
                        new EmptyBorder(Theme.LG, 0, 0, 0)));
        JPanel heading = Theme.panel(new BorderLayout(Theme.MD, 0), Theme.PAPER);
        heading.add(feedbackTitle, BorderLayout.CENTER);
        JPanel side = Theme.panel(new FlowLayout(FlowLayout.RIGHT, Theme.SM, 0), Theme.PAPER);
        side.add(feedbackState);
        AppButton dismiss = new AppButton("", LineIcon.Kind.CLOSE, AppButton.Style.GHOST);
        dismiss.setName("feedback.dismiss");
        dismiss.setToolTipText("Fechar mensagem");
        dismiss.getAccessibleContext().setAccessibleName("Fechar mensagem");
        dismiss.addActionListener(e -> dismissFeedback());
        side.add(dismiss);
        heading.add(side, BorderLayout.EAST);
        feedback.add(heading, BorderLayout.NORTH);
        feedbackText.setName("feedback.message");
        feedbackText.setRows(2);
        feedbackText.getAccessibleContext().setAccessibleName("Resultado ou mensagem da operação");
        JScrollPane scroll = Theme.scroll(feedbackText);
        feedback.add(scroll, BorderLayout.CENTER);
        feedback.setVisible(false);
    }

    private JPanel createStatus() {
        JPanel bar = Theme.panel(new BorderLayout(), Theme.PAPER);
        status.setName("workspace.status");
        status.putClientProperty("html.disable", true);
        status.getAccessibleContext().setAccessibleName("Estado da imagem");
        bar.add(status, BorderLayout.CENTER);
        return bar;
    }

    private void adaptLayout() {
        if (sidebar == null) return;
        boolean compact = compactOverride == null ? getWidth() < Theme.COMPACT_AT : compactOverride;
        sidebar.setCompact(compact);
        int available =
                getWidth() - (compact ? Theme.RAIL_WIDTH : Theme.SIDEBAR_WIDTH) - 2 * Theme.XL;
        int orientation =
                available < Theme.STACK_AT_WIDTH && getHeight() >= Theme.STACK_MIN_HEIGHT
                        ? JSplitPane.VERTICAL_SPLIT
                        : JSplitPane.HORIZONTAL_SPLIT;
        if (comparison.getOrientation() != orientation) {
            comparison.setOrientation(orientation);
            comparison.setDividerLocation(.5);
        }
        revalidate();
    }

    private void setFit(boolean value) {
        fit = value;
        original.setFit(fit);
        result.setFit(fit);
        fitButton.setText(fit ? "Ajustar à área" : "Tamanho real · 100%");
        fitButton
                .getAccessibleContext()
                .setAccessibleDescription(
                        fit
                                ? "Imagem ajustada à área disponível"
                                : "Um pixel da imagem por pixel da tela");
    }

    private void installShortcuts() {
        getRootPane()
                .registerKeyboardAction(
                        e -> {
                            if (feedback.isVisible()) dismissFeedback();
                        },
                        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                        JComponent.WHEN_IN_FOCUSED_WINDOW);
        int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        bind("open", KeyEvent.VK_O, shortcut);
        bind("save", KeyEvent.VK_S, shortcut);
        bind("undo", KeyEvent.VK_Z, shortcut);
        bind("reset", KeyEvent.VK_R, shortcut | InputEvent.SHIFT_DOWN_MASK);
        getRootPane()
                .registerKeyboardAction(
                        e -> {
                            compactOverride = !sidebar.isCompact();
                            adaptLayout();
                        },
                        KeyStroke.getKeyStroke(KeyEvent.VK_B, shortcut),
                        JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane()
                .registerKeyboardAction(
                        e -> setFit(true),
                        KeyStroke.getKeyStroke(KeyEvent.VK_0, shortcut),
                        JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane()
                .registerKeyboardAction(
                        e -> actions.get("about").actionPerformed(e),
                        KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
                        JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void dismissFeedback() {
        feedback.setVisible(false);
        if (fitButton.isEnabled()) fitButton.requestFocusInWindow();
        else openButton.requestFocusInWindow();
    }

    private void bind(String id, int key, int modifiers) {
        KeyStroke stroke = KeyStroke.getKeyStroke(key, modifiers);
        actions.get(id).putValue(Action.ACCELERATOR_KEY, stroke);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, id);
        getRootPane().getActionMap().put(id, actions.get(id));
    }

    private void updateState() {
        for (Action action : actions.values())
            action.setEnabled(!busy && (!imageActions.contains(action) || session.hasImage()));
        actions.get("undo").setEnabled(!busy && session.canUndo());
        fitButton.setEnabled(session.hasImage());
        for (int i = 0; i < getJMenuBar().getMenuCount(); i++)
            getJMenuBar().getMenu(i).setEnabled(!busy);
        if (!session.hasImage()) {
            status.setText("Nenhuma imagem carregada");
            return;
        }
        BufferedImage image = session.current();
        String size = image.getWidth() + " × " + image.getHeight() + " px";
        int count = session.historySize();
        String history =
                count == 1
                        ? "1 operação disponível para desfazer"
                        : count + " operações disponíveis para desfazer";
        status.setText(size + "  ·  " + history);
        status.getAccessibleContext()
                .setAccessibleDescription(arquivoAberto + " · " + status.getText());
        resultSize.setText(size);
        originalSize.setText(
                session.original().getWidth() + " × " + session.original().getHeight() + " px");
        document.setText(arquivoAberto);
        document.setToolTipText(arquivoAberto);
    }

    private void applyTransform(UnaryOperator<BufferedImage> operation) {
        if (busy || !session.hasImage()) return;
        execute(
                "Processando imagem…",
                () -> session.apply(operation),
                image -> {
                    result.setImage(image);
                    feedback.setVisible(false);
                    updateState();
                });
    }

    private void undo() {
        if (busy || !session.hasImage()) return;
        result.setImage(session.undo());
        feedback.setVisible(false);
        updateState();
    }

    private void reset() {
        if (busy || !session.hasImage()) return;
        result.setImage(session.reset());
        feedback.setVisible(false);
        updateState();
    }

    private void runAnalysis(String title, Function<BufferedImage, AnalysisResult> operation) {
        if (busy || !session.hasImage()) return;
        BufferedImage image = session.current();
        execute(
                "Analisando imagem…",
                () -> operation.apply(image),
                analysis -> {
                    String state =
                            switch (analysis.status()) {
                                case IDENTIFIED -> "Identificado";
                                case INCONCLUSIVE -> "Inconclusivo";
                                case NOT_FOUND -> "Não encontrado";
                            };
                    showFeedback(title, state, analysis.summary());
                });
    }

    private <T> void execute(String message, Callable<T> work, Consumer<T> success) {
        if (busy) return;
        Window active = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        if (active instanceof JDialog && active.getOwner() == this) active.dispose();
        busy = true;
        updateState();
        progress.setIndeterminate(true);
        progress.getAccessibleContext().setAccessibleDescription(message);
        status.setText(message);
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return work.call();
            }

            @Override
            protected void done() {
                T value = null;
                Throwable failure = null;
                try {
                    value = get();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    failure = ex;
                } catch (java.util.concurrent.ExecutionException ex) {
                    failure = ex.getCause();
                }
                busy = false;
                progress.setIndeterminate(false);
                progress.setValue(0);
                updateState();
                if (failure != null) showError(failure);
                else success.accept(value);
            }
        }.execute();
    }

    private void showFeedback(String title, String state, String text) {
        feedbackTitle.setText(title);
        feedbackState.setText(state);
        feedbackText.setText(text);
        feedbackText.setCaretPosition(0);
        feedback.setVisible(true);
        feedbackText.getAccessibleContext().setAccessibleDescription(state + ". " + text);
        feedback.revalidate();
        feedbackText.requestFocusInWindow();
        repaint();
    }

    private void showError(Throwable error) {
        LOG.log(Level.WARNING, "Falha na operação de imagem", error);
        String message =
                error instanceof LinkageError
                        ? "Este recurso não está disponível nesta plataforma. Consulte o guia de"
                                + " instalação."
                        : error.getMessage();
        showFeedback(
                "Não foi possível concluir",
                "Erro",
                message == null
                        ? "Tente novamente com outra imagem ou outros parâmetros."
                        : message);
    }

    private void openImage() {
        if (busy) return;
        FileDialog dialog = new FileDialog(this, "Abrir imagem", FileDialog.LOAD);
        dialog.setVisible(true);
        if (dialog.getFile() != null)
            carregarImagem(Path.of(dialog.getDirectory(), dialog.getFile()));
        dialog.dispose();
    }

    public void carregarImagem(Path path) {
        execute(
                "Carregando imagem…",
                () -> {
                    session.load(imageFiles.read(path));
                    return session.current();
                },
                image -> {
                    arquivoAberto = path.getFileName().toString();
                    original.setImage(session.original());
                    result.setImage(image);
                    feedback.setVisible(false);
                    cards.show(canvasCards, "image");
                    comparison.setDividerLocation(.5);
                    updateState();
                });
    }

    private void saveImage() {
        if (busy || !session.hasImage()) return;
        FileDialog dialog = new FileDialog(this, "Exportar PNG", FileDialog.SAVE);
        dialog.setFile("resultado.png");
        dialog.setVisible(true);
        if (dialog.getFile() == null) {
            dialog.dispose();
            return;
        }
        String name =
                dialog.getFile().toLowerCase(Locale.ROOT).endsWith(".png")
                        ? dialog.getFile()
                        : dialog.getFile() + ".png";
        Path path = Path.of(dialog.getDirectory(), name);
        dialog.dispose();
        if (Files.exists(path)
                && !Dialogs.confirm(
                        this,
                        "Substituir arquivo?",
                        "Já existe um arquivo com esse nome. Substituí-lo remove a versão salva"
                                + " anteriormente.",
                        "Substituir arquivo")) return;
        BufferedImage image = session.current();
        execute(
                "Exportando imagem…",
                () -> {
                    imageFiles.writePng(image, path);
                    return path;
                },
                saved ->
                        showFeedback(
                                "Imagem exportada",
                                "PNG",
                                saved.getFileName() + " foi salvo com sucesso."));
    }

    private void configureDrop() {
        TransferHandler handler =
                new TransferHandler() {
                    @Override
                    public boolean canImport(TransferSupport support) {
                        return !busy
                                && support.isDataFlavorSupported(
                                        java.awt.datatransfer.DataFlavor.javaFileListFlavor);
                    }

                    @Override
                    public boolean importData(TransferSupport support) {
                        if (!canImport(support)) return false;
                        try {
                            List<?> files =
                                    (List<?>)
                                            support.getTransferable()
                                                    .getTransferData(
                                                            java.awt.datatransfer.DataFlavor
                                                                    .javaFileListFlavor);
                            if (files.isEmpty() || !(files.get(0) instanceof File file))
                                return false;
                            carregarImagem(file.toPath());
                            return true;
                        } catch (Exception ex) {
                            showError(ex);
                            return false;
                        }
                    }
                };
        original.setTransferHandler(handler);
        canvasCards.setTransferHandler(handler);
        ((JPanel) canvasCards.getComponent(0)).setTransferHandler(handler);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(
                () -> {
                    Theme.install();
                    new Painel().setVisible(true);
                });
    }
}
