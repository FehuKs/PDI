package src;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.imageio.ImageIO;
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

    private BufferedImage originalImage;
    private BufferedImage transformedImage;
    private final Deque<BufferedImage> historico = new ArrayDeque<>();
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
    private final IdentificadorPlacas identificadorPlacas = new IdentificadorPlacas(grayscale, limiarizacao);
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
                    BufferedImage alvo = imagemAtual();
                    String resultado = leitorRelogio.lerHorario(alvo);
                    mostrarResultado("Horário identificado", resultado);
                }), "Ler Relógio Analógico")));

        // Ex 2 — Contagem de Objetos Coloridos
        menuExtracao.add(menuItem("Ex.2 — Contar Objetos Coloridos", e -> abrirDialogExtracao(
                new ContadorObjetosView(() -> {
                    if (!verificarImagem()) return;
                    String resultado = contadorObjetos.contar(imagemAtual());
                    mostrarResultado("Objetos por cor", resultado);
                }), "Contar Objetos Coloridos")));

        // Ex 3 — Reconhecimento de Letras
        menuExtracao.add(menuItem("Ex.3 — Reconhecer Letras (A–Z)", e -> abrirDialogExtracao(
                new ReconhecedorLetrasView(() -> {
                    if (!verificarImagem()) return;
                    String resultado = reconhecedorLetras.reconhecer(imagemAtual());
                    mostrarResultado("Letras encontradas", resultado);
                }), "Reconhecer Letras")));

        // Ex 4 — Identificação de Placas
        menuExtracao.add(menuItem("Ex.4 — Identificar Placas de Trânsito", e -> abrirDialogExtracao(
                new IdentificadorPlacasView(() -> {
                    if (!verificarImagem()) return;
                    String resultado = identificadorPlacas.identificar(imagemAtual());
                    mostrarResultado("Placas identificadas", resultado);
                }), "Identificar Placas de Trânsito")));

        // Ex 5 — Análise de Barras
        menuExtracao.add(menuItem("Ex.5 — Analisar Barras Verticais", e -> abrirDialogExtracao(
                new AnalisadorBarrasView(() -> {
                    if (!verificarImagem()) return;
                    String resultado = analisadorBarras.analisar(imagemAtual());
                    mostrarResultado("Análise de barras", resultado);
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
        BufferedImage atual = imagemAtual();
        historico.push(atual);
        btnDesfazer.setEnabled(true);
        transformedImage = fn.apply(atual);
        lblTransformedImg.setImagem(transformedImage);
        atualizarStatus();
    }

    private BufferedImage imagemAtual() {
        return transformedImage != null ? transformedImage : originalImage;
    }

    private void desfazer() {
        if (!historico.isEmpty()) {
            transformedImage = historico.pop();
            lblTransformedImg.setImagem(transformedImage);
            btnDesfazer.setEnabled(!historico.isEmpty());
            atualizarStatus();
        }
    }

    private void resetar() {
        if (originalImage != null) {
            transformedImage = null; historico.clear(); btnDesfazer.setEnabled(false);
            lblTransformedImg.setImagem(originalImage);
            atualizarStatus();
        }
    }

    private void atualizarStatus() {
        if (originalImage == null) { lblStatus.setText("Nenhuma imagem carregada"); return; }
        BufferedImage ref = imagemAtual();
        String info = arquivoAberto + "  —  " + ref.getWidth() + " × " + ref.getHeight() + " px";
        if (!historico.isEmpty()) info += "  ·  " + historico.size() + " operação(ões)";
        lblStatus.setText(info);
    }

  
    // Helpers
    
    private void abrirDialog(JPanel view, String titulo) {
        if (!verificarImagem()) return;
        JOptionPane.showMessageDialog(this, view, titulo, JOptionPane.PLAIN_MESSAGE);
    }

    /** Abre um dialog de extração sem verificar imagem (a View fará isso via callback) */
    private void abrirDialogExtracao(JPanel view, String titulo) {
        JOptionPane.showMessageDialog(this, view, titulo, JOptionPane.PLAIN_MESSAGE);
    }

    /** Exibe o resultado de um exercício de extração em uma janela de diálogo */
    private void mostrarResultado(String titulo, String resultado) {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblResultado = new JLabel(
            "<html><div style='font-size:14pt'><b>" + resultado + "</b></div></html>"
        );
        lblResultado.setHorizontalAlignment(SwingConstants.CENTER);
        painel.add(lblResultado, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this, painel, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean verificarImagem() {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Abra uma imagem primeiro.", "Sem imagem", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void abrirImagem() {
        FileDialog fd = new FileDialog(this, "Selecione a imagem", FileDialog.LOAD);
        fd.setVisible(true);
        if (fd.getFile() == null) return;
        try {
            originalImage    = ImageIO.read(new File(fd.getDirectory() + fd.getFile()));
            transformedImage = null; historico.clear(); btnDesfazer.setEnabled(false);
            arquivoAberto    = fd.getFile();
            lblOriginalImg.setImagem(originalImage);
            lblTransformedImg.setImagem(originalImage);
            atualizarStatus();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar a imagem.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void salvarImagem() {
        if (transformedImage == null) {
            JOptionPane.showMessageDialog(this, "Nenhuma imagem transformada para salvar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        FileDialog fd = new FileDialog(this, "Salvar imagem", FileDialog.SAVE);
        fd.setFile("*.png"); fd.setVisible(true);
        if (fd.getFile() == null) return;
        String nome = fd.getFile().toLowerCase().endsWith(".png") ? fd.getFile() : fd.getFile() + ".png";
        try {
            ImageIO.write(transformedImage, "png", new File(fd.getDirectory() + nome));
            JOptionPane.showMessageDialog(this, "Imagem salva com sucesso.", "Salvo", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarSobre() {
        JOptionPane.showMessageDialog(this,
            "<html><div style='text-align:center'>Processamento Digital de Imagens<br>"
            + "<small>Fernando Hubner Silva — v1.6</small></div></html>",
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