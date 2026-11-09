package src;

import src.core.AnalysisResult;
import src.core.EditorSession;
import src.infra.ImageFiles;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.*;
import javax.swing.border.*;

import src.desafio.AnalisadorBarras;
import src.desafio.AnalisadorBarrasView;
import src.desafio.ContadorObjetos;
import src.desafio.ContadorObjetosView;
import src.desafio.IdentificadorPlacas;
import src.desafio.IdentificadorPlacasView;
import src.desafio.LeitorRelogio;
import src.desafio.LeitorRelogioView;
import src.desafio.ReconhecedorLetras;
import src.desafio.ReconhecedorLetrasView;
import src.filtros.BordasdeSobel;
import src.filtros.Brilho;
import src.filtros.BrilhoView;
import src.filtros.Contraste;
import src.filtros.ContrasteView;
import src.filtros.FiltrodaMediana;
import src.filtros.FiltrodeGaus;
import src.filtros.Grayscale;
import src.filtros.Limiarizacao;
import src.filtros.LimiarizacaoView;
import src.filtros.Marrhildreth;
import src.filtros.Marrhildrethview;
import src.filtros.Robinson;
import src.morfologia.Abertura;
import src.morfologia.Dilatacao;
import src.morfologia.Erosao;
import src.morfologia.Fechamento;
import src.afinamento.ZhangSuen;
import src.afinamento.ZhangSuenView;
import src.transformacoes.Escala;
import src.transformacoes.EscalaView;
import src.transformacoes.Espelhamento;
import src.transformacoes.EspelhamentoView;
import src.transformacoes.Rotacionar;
import src.transformacoes.RotacionarView;
import src.transformacoes.Transladar;
import src.transformacoes.TransladarView;

public class Painel extends JFrame {

    private static final Color COR_BG_PRIMARIO   = new Color(0xFFFFFF);
    private static final Color COR_BG_SECUNDARIO = new Color(0xF5F5F3);
    private static final Color COR_BG_CANVAS     = new Color(0xEBEAE6);
    private static final Color COR_BORDA         = new Color(0xD3D1C7);
    private static final Color COR_TEXTO_PRIMARIO= new Color(0x2C2C2A);
    private static final Color COR_TEXTO_MUTED   = new Color(0x888780);
    private static final Color COR_ACCENT        = new Color(0x185FA5);

    private final EditorSession session = new EditorSession();
    private final ImageFiles imageFiles = new ImageFiles();
    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(Painel.class.getName());
    private boolean busy;
    private final java.util.Map<Component, Boolean> enabledBeforeWork = new java.util.IdentityHashMap<>();
    private String arquivoAberto = "";

    private ImagemPanel lblOriginalImg;
    private ImagemPanel lblTransformedImg;
    private JButton btnDesfazer;
    private JLabel lblStatus;

    // Filtros / Transformações
    private final Rotacionar      rotacionar    = new Rotacionar();
    private final Transladar      transladar    = new Transladar();
    private final Espelhamento    espelhamento  = new Espelhamento();
    private final Escala          escala        = new Escala();
    private final Grayscale       grayscale     = new Grayscale();
    private final Brilho          brilho        = new Brilho();
    private final Contraste       contraste     = new Contraste();
    private final Limiarizacao    limiarizacao  = new Limiarizacao();
    private final FiltrodaMediana filtroMediana = new FiltrodaMediana();
    private final FiltrodeGaus    filtroGaus    = new FiltrodeGaus();
    private final BordasdeSobel   bordasSobel   = new BordasdeSobel();
    private final Robinson        robinson      = new Robinson();
    private final Dilatacao       dilatacao     = new Dilatacao();
    private final Erosao          erosao        = new Erosao();
    private final Abertura        abertura      = new Abertura();
    private final Fechamento      fechamento    = new Fechamento();
    private final ZhangSuen       zhangSuen     = new ZhangSuen();

    // Extração de Características (Desafio)
    // Injeção das classes existentes do projeto nas classes de extração
    private final LeitorRelogio      leitorRelogio      = new LeitorRelogio(grayscale, limiarizacao, zhangSuen);
    private final ContadorObjetos    contadorObjetos    = new ContadorObjetos(filtroMediana);
    private final ReconhecedorLetras reconhecedorLetras = new ReconhecedorLetras(grayscale, limiarizacao, filtroMediana);
    private final IdentificadorPlacas identificadorPlacas = new IdentificadorPlacas();
    private final AnalisadorBarras   analisadorBarras   = new AnalisadorBarras(grayscale, limiarizacao);

    public Painel() {
        super("Sistema de Processamento de Imagens");
        setSize(1100, 720);
        setMinimumSize(new Dimension(800, 550));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(COR_BG_PRIMARIO);
        configurarMenuBar();
        configurarCanvasArea();
        configurarStatusBar();
        configurarArrastarImagem();
        // Abre a janela já maximizada (tela cheia)
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void configurarMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(COR_BG_PRIMARIO);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COR_BORDA));

        // Arquivo
        JMenu menuArquivo = menuEstilizado("Arquivo");
        menuArquivo.add(menuItem("Abrir imagem",  e -> abrirImagem()));
        menuArquivo.add(menuItem("Salvar imagem", e -> salvarImagem()));
        menuArquivo.add(menuItem("Sobre",         e -> mostrarSobre()));
        menuArquivo.addSeparator();
        menuArquivo.add(menuItem("Sair",          e -> System.exit(0)));

        // Transformações Geométricas
        JMenu menuTransformacoes = menuEstilizado("Transformações Geométricas");
        menuTransformacoes.add(menuItem("Rotacionar", e -> abrirDialog(
                new RotacionarView(p -> aplicarTransformacao(img -> rotacionar.rotacionarImagem(img, p[0], p[1]==1))), "Rotacionar")));
        menuTransformacoes.add(menuItem("Transladar", e -> abrirDialog(
                new TransladarView(p -> aplicarTransformacao(img -> transladar.transladarImagem(img, p[0], p[1]))), "Transladar")));
        menuTransformacoes.add(menuItem("Espelhar", e -> abrirDialog(
                new EspelhamentoView(p -> aplicarTransformacao(img -> espelhamento.espelharImagem(img, p[0], p[1]))), "Espelhar")));
        menuTransformacoes.add(menuItem("Escala", e -> abrirDialog(
                new EscalaView(f -> aplicarTransformacao(img -> escala.aplicarEscala(img, f))), "Escala")));

        // Filtros
        JMenu menuFiltros = menuEstilizado("Filtros");
        menuFiltros.add(menuItem("Grayscale", e -> { if(verificarImagem()) aplicarTransformacao(img -> grayscale.aplicarGrayscale(img)); }));
        menuFiltros.add(menuItem("Mediana",   e -> { if(verificarImagem()) aplicarTransformacao(img -> filtroMediana.aplicarFiltroMediana(img)); }));
        menuFiltros.add(menuItem("Gaussiano", e -> { if(verificarImagem()) aplicarTransformacao(img -> filtroGaus.aplicarFiltro(img)); }));
        menuFiltros.add(menuItem("Sobel",     e -> { if(verificarImagem()) aplicarTransformacao(img -> bordasSobel.aplicarFiltro(img)); }));
        menuFiltros.add(menuItem("Robinson",  e -> { if(verificarImagem()) aplicarTransformacao(img -> robinson.aplicarFiltro(img)); }));
        menuFiltros.add(menuItem("Marr-Hildreth (LoG)", e -> abrirDialog(
                new Marrhildrethview((sigma, limiar) ->
                    aplicarTransformacao(img -> new Marrhildreth(sigma, limiar).aplicarFiltro(img))), "Marr-Hildreth")));
        menuFiltros.addSeparator();
        menuFiltros.add(menuItem("Brilho",    e -> abrirDialog(new BrilhoView(v -> aplicarTransformacao(img -> brilho.aplicarBrilho(img, v))), "Brilho")));
        menuFiltros.add(menuItem("Contraste", e -> abrirDialog(new ContrasteView(f -> aplicarTransformacao(img -> contraste.aplicarContraste(img, f))), "Contraste")));
        menuFiltros.add(menuItem("Threshold", e -> abrirDialog(new LimiarizacaoView(t -> aplicarTransformacao(img -> limiarizacao.aplicarLimiarizacao(img, t))), "Threshold")));

        // Morfologia
        JMenu menuMorfologia = menuEstilizado("Morfologia Matemática");
        menuMorfologia.add(menuItem("Dilatação",  e -> { if(verificarImagem()) aplicarTransformacao(img -> dilatacao.aplicarFiltro(img)); }));
        menuMorfologia.add(menuItem("Erosão",     e -> { if(verificarImagem()) aplicarTransformacao(img -> erosao.aplicarFiltro(img)); }));
        menuMorfologia.add(menuItem("Abertura",   e -> { if(verificarImagem()) aplicarTransformacao(img -> abertura.aplicarFiltro(img)); }));
        menuMorfologia.add(menuItem("Fechamento", e -> { if(verificarImagem()) aplicarTransformacao(img -> fechamento.aplicarFiltro(img)); }));

        // Afinamento
        JMenu menuAfinamento = menuEstilizado("Afinamento");
        menuAfinamento.add(menuItem("Zhang-Suen", e -> abrirDialog(
                new ZhangSuenView(() -> aplicarTransformacao(img -> zhangSuen.aplicarThinning(img))), "Zhang-Suen")));

        // ----------------------------------------------------------------
        // Extração de Características — DESAFIO
        // ----------------------------------------------------------------
        JMenu menuExtracao = menuEstilizado("Exercícios");

        // Ex 1 — Relógio Analógico
        menuExtracao.add(menuItem("Ex.1 — Ler Relógio Analógico", e -> abrirDialogExtracao(
                new LeitorRelogioView(() -> {
                    if (!verificarImagem()) return;
                    executarAnalise("Horário identificado", leitorRelogio::evaluate);
                }), "Ler Relógio Analógico")));

        // Ex 2 — Contagem de Objetos Coloridos
        menuExtracao.add(menuItem("Ex.2 — Contar Objetos Coloridos", e -> abrirDialogExtracao(
                new ContadorObjetosView(() -> {
                    if (!verificarImagem()) return;
                    executarAnalise("Objetos por cor", contadorObjetos::evaluate);
                }), "Contar Objetos Coloridos")));

        // Ex 3 — Reconhecimento de Letras
        menuExtracao.add(menuItem("Ex.3 — Reconhecer Letras (A–Z)", e -> abrirDialogExtracao(
                new ReconhecedorLetrasView(() -> {
                    if (!verificarImagem()) return;
                    executarAnalise("Letras encontradas", reconhecedorLetras::evaluate);
                }), "Reconhecer Letras")));

        // Ex 4 — Identificação de Placas
        menuExtracao.add(menuItem("Ex.4 — Identificar Placas de Trânsito", e -> abrirDialogExtracao(
                new IdentificadorPlacasView(() -> {
                    if (!verificarImagem()) return;
                    executarAnalise("Placas identificadas", identificadorPlacas::evaluate);
                }), "Identificar Placas de Trânsito")));

        // Ex 5 — Análise de Barras
        menuExtracao.add(menuItem("Ex.5 — Analisar Barras Verticais", e -> abrirDialogExtracao(
                new AnalisadorBarrasView(() -> {
                    if (!verificarImagem()) return;
                    executarAnalise("Análise de barras", analisadorBarras::evaluate);
                }), "Analisar Barras Verticais")));

        menuBar.add(menuArquivo);
        menuBar.add(menuTransformacoes);
        menuBar.add(menuFiltros);
        menuBar.add(menuMorfologia);
        menuBar.add(menuAfinamento);
        menuBar.add(menuExtracao);
        setJMenuBar(menuBar);
    }

    // ----------------------------------------------------------------
    // Canvas
    // ----------------------------------------------------------------
    private void configurarCanvasArea() {
        JPanel paneOriginal     = criarPainelImagem("Original");
        JPanel paneTransformado = criarPainelImagem("Resultado");
        lblOriginalImg    = labelImagem(paneOriginal);
        lblTransformedImg = labelImagem(paneTransformado);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, paneOriginal, paneTransformado);
        split.setDividerLocation(0.5); split.setResizeWeight(0.5);
        split.setDividerSize(1); split.setBorder(null); split.setBackground(COR_BG_CANVAS);
        add(split, BorderLayout.CENTER);
    }

    private JPanel criarPainelImagem(String titulo) {
        JPanel pane = new JPanel(new BorderLayout());
        pane.setBackground(COR_BG_CANVAS);
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        lbl.setForeground(COR_TEXTO_MUTED);
        lbl.setBorder(new EmptyBorder(8,12,8,12));
        lbl.setOpaque(true); lbl.setBackground(COR_BG_CANVAS);
        pane.add(lbl, BorderLayout.NORTH);
        return pane;
    }

    private ImagemPanel labelImagem(JPanel pane) {
        ImagemPanel painel = new ImagemPanel(COR_BG_CANVAS);
        pane.add(painel, BorderLayout.CENTER);
        return painel;
    }


    private static class ImagemPanel extends JPanel {
        private BufferedImage imagem;

        ImagemPanel(Color fundo) {
            setBackground(fundo);
        }

        void setImagem(BufferedImage img) {
            this.imagem = img;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (imagem == null) return;

            int pw = getWidth();
            int ph = getHeight();
            int iw = imagem.getWidth();
            int ih = imagem.getHeight();
            if (pw <= 0 || ph <= 0 || iw <= 0 || ih <= 0) return;

            double escala = Math.min((double) pw / iw, (double) ph / ih);
            int novaW = Math.max(1, (int) Math.round(iw * escala));
            int novaH = Math.max(1, (int) Math.round(ih * escala));
            int x = (pw - novaW) / 2;
            int y = (ph - novaH) / 2;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.drawImage(imagem, x, y, novaW, novaH, null);
            g2.dispose();
        }
    }

    // ----------------------------------------------------------------
    // Status bar
    // ----------------------------------------------------------------
    private void configurarStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(COR_BG_PRIMARIO);
        bar.setBorder(BorderFactory.createMatteBorder(1,0,0,0,COR_BORDA));
        bar.setPreferredSize(new Dimension(0,40));

        JPanel esquerda = new JPanel(new FlowLayout(FlowLayout.LEFT,6,6));
        esquerda.setOpaque(false);
        btnDesfazer = statusBotao("↩  Desfazer");
        btnDesfazer.setEnabled(false);
        btnDesfazer.addActionListener(e -> desfazer());
        JButton btnResetar = statusBotao("Resetar");
        btnResetar.addActionListener(e -> resetar());
        esquerda.add(btnDesfazer); esquerda.add(btnResetar);

        lblStatus = new JLabel("Nenhuma imagem carregada");
        lblStatus.setFont(new Font(Font.SANS_SERIF, Font.PLAIN,12));
        lblStatus.setForeground(COR_TEXTO_MUTED);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,6));
        direita.setOpaque(false);
        JButton btnSalvar = statusBotao("Salvar imagem");
        btnSalvar.setForeground(COR_ACCENT);
        btnSalvar.addActionListener(e -> salvarImagem());
        direita.add(btnSalvar);

        bar.add(esquerda, BorderLayout.WEST);
        bar.add(lblStatus, BorderLayout.CENTER);
        bar.add(direita, BorderLayout.EAST);
        add(bar, BorderLayout.SOUTH);
    }

    // ----------------------------------------------------------------
    // Lógica de transformação
    // ----------------------------------------------------------------
    private void aplicarTransformacao(java.util.function.UnaryOperator<BufferedImage> fn) {
        if (!verificarImagem()) return;
        executar("Processando imagem…", () -> session.apply(fn), image -> {
            lblTransformedImg.setImagem(image);
            atualizarStatus();
        });
    }

    private BufferedImage imagemAtual() { return session.current(); }

    private void desfazer() {
        if (busy || !session.hasImage()) return;
        lblTransformedImg.setImagem(session.undo());
        atualizarStatus();
    }

    private void resetar() {
        if (busy || !session.hasImage()) return;
        lblTransformedImg.setImagem(session.reset());
        atualizarStatus();
    }

    private void atualizarStatus() {
        btnDesfazer.setEnabled(!busy && session.canUndo());
        if (!session.hasImage()) { lblStatus.setText("Nenhuma imagem carregada"); return; }
        BufferedImage image = session.current();
        lblStatus.setText(arquivoAberto + "  —  " + image.getWidth() + " × " + image.getHeight()
                + " px  ·  " + session.historySize() + " operação(ões) disponíveis para desfazer");
    }

    private void executarAnalise(String title, java.util.function.Function<BufferedImage, AnalysisResult> analysis) {
        if (!verificarImagem()) return;
        BufferedImage image = session.current();
        executar("Analisando imagem…", () -> analysis.apply(image), result -> {
            atualizarStatus();
            JOptionPane.showMessageDialog(this, result.summary(), title,
                    result.status() == AnalysisResult.Status.IDENTIFIED
                            ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        });
    }

    private <T> void executar(String message, java.util.concurrent.Callable<T> work, java.util.function.Consumer<T> success) {
        if (busy) return;
        Window active = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        if (active instanceof JDialog && active.getOwner() == this) active.dispose();
        busy = true;
        enabledBeforeWork.clear();
        habilitarDuranteTrabalho(getContentPane(), false);
        habilitarDuranteTrabalho(getJMenuBar(), false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        lblStatus.setText(message);
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return work.call(); }
            @Override protected void done() {
                busy = false;
                enabledBeforeWork.forEach(Component::setEnabled);
                enabledBeforeWork.clear();
                setCursor(Cursor.getDefaultCursor());
                atualizarStatus();
                try { success.accept(get()); }
                catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                catch (java.util.concurrent.ExecutionException ex) { mostrarErro(ex.getCause()); }
            }
        }.execute();
    }

    private void habilitarDuranteTrabalho(Component component, boolean enabled) {
        if (component instanceof AbstractButton || component instanceof JTextField || component instanceof JComboBox) {
            enabledBeforeWork.put(component, component.isEnabled());
            component.setEnabled(enabled);
        }
        if (component instanceof JMenu menu) {
            for (Component child : menu.getMenuComponents()) habilitarDuranteTrabalho(child, enabled);
        } else if (component instanceof Container container) {
            for (Component child : container.getComponents()) habilitarDuranteTrabalho(child, enabled);
        }
    }

    private void mostrarErro(Throwable error) {
        LOG.log(java.util.logging.Level.WARNING, "Falha na operação de imagem", error);
        String message = error instanceof LinkageError ? "Não foi possível carregar o OpenCV para esta plataforma. Consulte o README."
                : error.getMessage() == null ? "Não foi possível concluir a operação." : error.getMessage();
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private void abrirDialog(JPanel view, String titulo) {
        if (!verificarImagem()) return;
        JOptionPane.showMessageDialog(this, view, titulo, JOptionPane.PLAIN_MESSAGE);
    }

    /** Abre um dialog de extração sem verificar imagem (a View fará isso via callback) */
    private void abrirDialogExtracao(JPanel view, String titulo) {
        JOptionPane.showMessageDialog(this, view, titulo, JOptionPane.PLAIN_MESSAGE);
    }

    private boolean verificarImagem() {
        if (busy) return false;
        if (!session.hasImage()) {
            JOptionPane.showMessageDialog(this, "Abra uma imagem primeiro.", "Sem imagem", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void abrirImagem() {
        if (busy) return;
        FileDialog fd = new FileDialog(this, "Selecione a imagem", FileDialog.LOAD);
        fd.setVisible(true);
        if (fd.getFile() != null) carregarImagem(java.nio.file.Path.of(fd.getDirectory(), fd.getFile()));
        fd.dispose();
    }

    public void carregarImagem(java.nio.file.Path path) {
        executar("Carregando imagem…", () -> { session.load(imageFiles.read(path)); return session.current(); }, image -> {
            arquivoAberto = path.getFileName().toString();
            lblOriginalImg.setImagem(session.original());
            lblTransformedImg.setImagem(session.current());
            atualizarStatus();
        });
    }

    private void salvarImagem() {
        if (!verificarImagem()) return;
        FileDialog fd = new FileDialog(this, "Salvar imagem", FileDialog.SAVE);
        fd.setFile("resultado.png"); fd.setVisible(true);
        if (fd.getFile() == null) { fd.dispose(); return; }
        String name = fd.getFile().toLowerCase(java.util.Locale.ROOT).endsWith(".png") ? fd.getFile() : fd.getFile() + ".png";
        java.nio.file.Path path = java.nio.file.Path.of(fd.getDirectory(), name);
        fd.dispose();
        if (java.nio.file.Files.exists(path) && JOptionPane.showConfirmDialog(this,
                "Substituir o arquivo existente?", "Salvar imagem", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        BufferedImage image = session.current();
        executar("Salvando imagem…", () -> { imageFiles.writePng(image, path); return path; },
                saved -> JOptionPane.showMessageDialog(this, "Imagem salva com sucesso.", "Salvo", JOptionPane.INFORMATION_MESSAGE));
    }

    private void configurarArrastarImagem() {
        lblOriginalImg.setTransferHandler(new TransferHandler() {
            @Override public boolean canImport(TransferSupport support) {
                return !busy && support.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.javaFileListFlavor);
            }
            @Override public boolean importData(TransferSupport support) {
                if (!canImport(support)) return false;
                try {
                    java.util.List<?> files = (java.util.List<?>) support.getTransferable()
                            .getTransferData(java.awt.datatransfer.DataFlavor.javaFileListFlavor);
                    if (files.isEmpty() || !(files.get(0) instanceof File file)) return false;
                    carregarImagem(file.toPath());
                    return true;
                } catch (Exception ex) { mostrarErro(ex); return false; }
            }
        });
    }

    private void mostrarSobre() {
        JOptionPane.showMessageDialog(this,
            "<html><div style='text-align:center'>Processamento Digital de Imagens<br>"
            + "<small>Fernando Hubner Silva — v1.7</small></div></html>",
            "Sobre", JOptionPane.INFORMATION_MESSAGE);
    }

    // ----------------------------------------------------------------
    // Fábrica de componentes
    // ----------------------------------------------------------------
    private JMenu menuEstilizado(String nome) {
        JMenu m = new JMenu(nome);
        m.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        m.setForeground(COR_TEXTO_PRIMARIO);
        return m;
    }

    private JMenuItem menuItem(String nome, java.awt.event.ActionListener al) {
        JMenuItem item = new JMenuItem(nome);
        item.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        item.addActionListener(al);
        return item;
    }

    private JButton statusBotao(String nome) {
        JButton btn = new JButton(nome);
        btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        btn.setForeground(COR_TEXTO_PRIMARIO);
        btn.setBackground(COR_BG_PRIMARIO);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA, 1), new EmptyBorder(2,10,2,10)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(COR_BG_SECUNDARIO); }
            public void mouseExited (java.awt.event.MouseEvent e) { btn.setBackground(COR_BG_PRIMARIO); }
        });
        return btn;
    }

    public static void main(String[] args) {


        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new Painel().setVisible(true));
    }
}
