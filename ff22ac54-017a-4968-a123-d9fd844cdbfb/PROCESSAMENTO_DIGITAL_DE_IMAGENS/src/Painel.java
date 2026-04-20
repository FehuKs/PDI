package src;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

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
import src.filtros.Robinson;
import src.transformacoes.Escala;
import src.transformacoes.EscalaView;
import src.transformacoes.Espelhamento;
import src.transformacoes.EspelhamentoView;
import src.transformacoes.Rotacionar;
import src.transformacoes.RotacionarView;
import src.transformacoes.Transladar;
import src.transformacoes.TransladarView;

public class Painel extends JFrame {

    // --- Componentes de exibição ---
    private JLabel imageRaw;
    private JLabel lblTransformedImage;
    private JButton resetButton;
    private JButton undoButton;

    // --- Metadados da imagem ---
    private JLabel lblMetadados;

    // --- Histórico para desfazer (undo) ---
    private final Deque<BufferedImage> historico = new ArrayDeque<>();

    // --- Estado da imagem ---
    private BufferedImage originalImage;
    private BufferedImage transformedImage;
    private File arquivoAtual;

    // --- Instâncias das transformações ---
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

    public Painel() {
        super("Sistema de Processamento de Imagens");
        setSize(1100, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Barra de menus
        JMenuBar menuBar = new JMenuBar();

        JMenu menuArquivo = new JMenu("Arquivo");
        JMenuItem menuItemAbrir  = new JMenuItem("Abrir imagem");
        JMenuItem menuItemSalvar = new JMenuItem("Salvar imagem");
        JMenuItem menuItemSobre  = new JMenuItem("Sobre");
        JMenuItem menuItemSair   = new JMenuItem("Sair");

        menuItemAbrir.addActionListener(e  -> abrirImagem());
        menuItemSalvar.addActionListener(e -> salvarImagem());
        menuItemSair.addActionListener(e   -> System.exit(0));
        menuItemSobre.addActionListener(e  -> JOptionPane.showMessageDialog(Painel.this,
                "<html><div style='text-align: center;'>Projeto de Processamento Digital de Imagens<br>"
                + "Desenvolvido por Fernando Hubner Silva - versão 1.3</div></html>",
                "Sobre", JOptionPane.INFORMATION_MESSAGE));

        menuArquivo.add(menuItemAbrir);
        menuArquivo.add(menuItemSalvar);
        menuArquivo.add(menuItemSobre);
        menuArquivo.addSeparator();
        menuArquivo.add(menuItemSair);


		// Menu Transformações Geométricas

		JMenu menuTransformacoes = new JMenu("Transformações Geométricas");

        menuTransformacoes.add(new JMenuItem(new AbstractAction("Rotacionar") {
            @Override public void actionPerformed(ActionEvent e) {
                if (!verificarImagem()) return;
                RotacionarView rotationView = new RotacionarView(params -> {
                    int angle = params[0]; boolean isRight = params[1] == 1;
                    aplicarTransformacao(img -> rotacionar.rotacionarImagem(img, angle, isRight));
                });
                JOptionPane.showMessageDialog(Painel.this, rotationView,
                        "Parâmetros de Rotação", JOptionPane.PLAIN_MESSAGE);
            }
        }));

        menuTransformacoes.add(new JMenuItem(new AbstractAction("Transladar") {
            @Override public void actionPerformed(ActionEvent e) {
                if (!verificarImagem()) return;
                TransladarView transladarView = new TransladarView(params -> {
                    int dx = params[0]; int dy = params[1];
                    aplicarTransformacao(img -> transladar.transladarImagem(img, dx, dy));
                });
                JOptionPane.showMessageDialog(Painel.this, transladarView,
                        "Parâmetros de Translação", JOptionPane.PLAIN_MESSAGE);
            }
        }));

        menuTransformacoes.add(new JMenuItem(new AbstractAction("Espelhar") {
            @Override public void actionPerformed(ActionEvent e) {
                if (!verificarImagem()) return;
                EspelhamentoView ev = new EspelhamentoView(params -> {
                    boolean h = params[0]; boolean v = params[1];
                    aplicarTransformacao(img -> espelhamento.espelharImagem(img, h, v));
                });
                JOptionPane.showMessageDialog(Painel.this, ev,
                        "Parâmetros de Espelhamento", JOptionPane.PLAIN_MESSAGE);
            }
        }));

        menuTransformacoes.add(new JMenuItem(new AbstractAction("Aumentar/Diminuir (Escala)") {
            @Override public void actionPerformed(ActionEvent e) {
                if (!verificarImagem()) return;
                EscalaView ev = new EscalaView(fator ->
                    aplicarTransformacao(img -> escala.aplicarEscala(img, fator))
                );
                JOptionPane.showMessageDialog(Painel.this, ev,
                        "Parâmetros de Escala", JOptionPane.PLAIN_MESSAGE);
            }
        }));


		// Menu Filtros

		JMenu menuFiltros = new JMenu("Filtros");

        menuFiltros.add(new JMenuItem(new AbstractAction("Grayscale") {
            @Override public void actionPerformed(ActionEvent e) {
                if (!verificarImagem()) return;
                aplicarTransformacao(img -> grayscale.aplicarGrayscale(img));
            }
        }));
        menuFiltros.add(new JMenuItem(new AbstractAction("Brilho") {
            @Override public void actionPerformed(ActionEvent e) {
                if (!verificarImagem()) return;
                BrilhoView bv = new BrilhoView(valor ->
                    aplicarTransformacao(img -> brilho.aplicarBrilho(img, valor))
                );
                JOptionPane.showMessageDialog(Painel.this, bv, "Ajuste de Brilho", JOptionPane.PLAIN_MESSAGE);
            }
        }));
        menuFiltros.add(new JMenuItem(new AbstractAction("Contraste") {
            @Override public void actionPerformed(ActionEvent e) {
                if (!verificarImagem()) return;
                ContrasteView cv = new ContrasteView(fator ->
                    aplicarTransformacao(img -> contraste.aplicarContraste(img, fator))
                );
                JOptionPane.showMessageDialog(Painel.this, cv, "Ajuste de Contraste", JOptionPane.PLAIN_MESSAGE);
            }
        }));
        menuFiltros.add(new JMenuItem(new AbstractAction("Threshold") {
            @Override public void actionPerformed(ActionEvent e) {
                if (!verificarImagem()) return;
                LimiarizacaoView lv = new LimiarizacaoView(threshold ->
                    aplicarTransformacao(img -> limiarizacao.aplicarLimiarizacao(img, threshold))
                );
                JOptionPane.showMessageDialog(Painel.this, lv, "Ajuste de Limiar", JOptionPane.PLAIN_MESSAGE);
            }
        }));
        menuFiltros.add(new JMenuItem(new AbstractAction("Filtro de Mediana") {
            @Override public void actionPerformed(ActionEvent e) {
                if (!verificarImagem()) return;
                aplicarTransformacao(img -> filtroMediana.aplicarFiltroMediana(img));
            }
        }));
        menuFiltros.add(new JMenuItem(new AbstractAction("Filtro Gaussiano") {
            @Override public void actionPerformed(ActionEvent e) {
                if (!verificarImagem()) return;
                aplicarTransformacao(img -> filtroGaus.aplicarFiltro(img));
            }
        }));
        menuFiltros.add(new JMenuItem(new AbstractAction("Detecção de Bordas - Sobel") {
            @Override public void actionPerformed(ActionEvent e) {
                if (!verificarImagem()) return;
                aplicarTransformacao(img -> bordasSobel.aplicarFiltro(img));
            }
        }));
        menuFiltros.add(new JMenuItem(new AbstractAction("Filtro de Robinson") {
            @Override public void actionPerformed(ActionEvent e) {
                if (!verificarImagem()) return;
                aplicarTransformacao(img -> robinson.aplicarFiltro(img));
            }
        }));

        // Menu Morfologia e Extração (placeholders)

        JMenu menuMorfologia = new JMenu("Morfologia Matemática");
        menuMorfologia.add(new JMenuItem("Dilatação"));
        menuMorfologia.add(new JMenuItem("Erosão"));
        menuMorfologia.add(new JMenuItem("Abertura"));
        menuMorfologia.add(new JMenuItem("Fechamento"));

        JMenu menuExtracao = new JMenu("Extração de Características");
        menuExtracao.add(new JMenuItem("Desafio"));

        menuBar.add(menuArquivo);
        menuBar.add(menuTransformacoes);
        menuBar.add(menuFiltros);
        menuBar.add(menuMorfologia);
        menuBar.add(menuExtracao);
        setJMenuBar(menuBar);

        // Painéis de imagem

        JPanel originalPanel    = new JPanel(new GridBagLayout());
        JPanel transformedPanel = new JPanel(new GridBagLayout());

        imageRaw            = new JLabel();
        lblTransformedImage = new JLabel();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        originalPanel.add(imageRaw, gbc);
        transformedPanel.add(lblTransformedImage, gbc);

        // Drag & Drop na área da imagem original
        configurarDragAndDrop(originalPanel);

        JScrollPane scrollOriginal    = new JScrollPane(originalPanel);
        JScrollPane scrollTransformed = new JScrollPane(transformedPanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                scrollOriginal, scrollTransformed);
        splitPane.setDividerLocation(500);
        add(splitPane, BorderLayout.CENTER);

        // Painel inferior
        JPanel bottomWrapper = new JPanel(new BorderLayout());

        // Metadados
        lblMetadados = new JLabel("  Arraste uma imagem aqui ou use Arquivo \u2192 Abrir",
                SwingConstants.CENTER);
        lblMetadados.setFont(new Font("Monospaced", Font.PLAIN, 11));
        lblMetadados.setForeground(Color.DARK_GRAY);
        lblMetadados.setBorder(new EmptyBorder(3, 8, 3, 8));

        // Controles
        JPanel controlsPanel = new JPanel(new GridBagLayout());
        controlsPanel.setBorder(new EmptyBorder(4, 8, 4, 8));
        GridBagConstraints gbcCtrl = new GridBagConstraints();

        JLabel lblOriginalTitle = new JLabel("Imagem Original",  SwingConstants.CENTER);
        JLabel lblModifiedTitle = new JLabel("Imagem Modificada", SwingConstants.CENTER);

        resetButton = new JButton("Resetar Imagem");
        resetButton.setPreferredSize(new Dimension(150, 25));
        resetButton.addActionListener(e -> resetImage());

        undoButton = new JButton("\u21A9 Desfazer");
        undoButton.setPreferredSize(new Dimension(120, 25));
        undoButton.setEnabled(false);
        undoButton.addActionListener(e -> desfazer());

        gbcCtrl.gridx = 0; gbcCtrl.gridy = 0;
        gbcCtrl.anchor = GridBagConstraints.WEST;
        gbcCtrl.fill = GridBagConstraints.HORIZONTAL;
        gbcCtrl.weightx = 1.0;
        controlsPanel.add(lblOriginalTitle, gbcCtrl);

        gbcCtrl.gridx = 1; gbcCtrl.anchor = GridBagConstraints.CENTER;
        gbcCtrl.fill = GridBagConstraints.NONE; gbcCtrl.weightx = 0;
        controlsPanel.add(undoButton, gbcCtrl);

        gbcCtrl.gridx = 2;
        controlsPanel.add(resetButton, gbcCtrl);

        gbcCtrl.gridx = 3;
        gbcCtrl.anchor = GridBagConstraints.EAST;
        gbcCtrl.fill = GridBagConstraints.HORIZONTAL;
        gbcCtrl.weightx = 1.0;
        controlsPanel.add(lblModifiedTitle, gbcCtrl);

        bottomWrapper.add(lblMetadados, BorderLayout.NORTH);
        bottomWrapper.add(controlsPanel, BorderLayout.CENTER);

        add(bottomWrapper, BorderLayout.SOUTH);
    }

    // Drag & Drop via DropTarget
    private void configurarDragAndDrop(JPanel panel) {
        new DropTarget(panel, DnDConstants.ACTION_COPY, new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    panel.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 200), 3));
                    panel.setBackground(new Color(230, 240, 255));
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                } else {
                    dtde.rejectDrag();
                }
            }
            @Override
            public void dragExit(DropTargetEvent dte) {
                panel.setBorder(null);
                panel.setBackground(null);
            }
            @Override
            public void drop(DropTargetDropEvent dtde) {
                panel.setBorder(null);
                panel.setBackground(null);
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>) dtde.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) carregarArquivo(files.get(0));
                    dtde.dropComplete(true);
                } catch (Exception ex) {
                    dtde.dropComplete(false);
                    JOptionPane.showMessageDialog(Painel.this,
                            "Erro ao carregar imagem arrastada.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }, true);
    }

    // Métodos auxiliares

    private boolean verificarImagem() {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this,
                    "Nenhuma imagem carregada.", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void aplicarTransformacao(java.util.function.UnaryOperator<BufferedImage> transformacao) {
        BufferedImage atual = (transformedImage != null) ? transformedImage : originalImage;
        historico.push(atual);
        undoButton.setEnabled(true);
        transformedImage = transformacao.apply(atual);
        lblTransformedImage.setIcon(new ImageIcon(transformedImage));
    }

    private void desfazer() {
        if (!historico.isEmpty()) {
            transformedImage = historico.pop();
            lblTransformedImage.setIcon(new ImageIcon(transformedImage));
            undoButton.setEnabled(!historico.isEmpty());
        }
    }

    private void resetImage() {
        if (originalImage != null) {
            transformedImage = null;
            historico.clear();
            undoButton.setEnabled(false);
            lblTransformedImage.setIcon(new ImageIcon(originalImage));
        }
    }

    // Carrega imagem a partir de File
    
    private void carregarArquivo(File file) {
        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) {
                JOptionPane.showMessageDialog(this,
                        "Formato de imagem não suportado.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            originalImage    = img;
            transformedImage = null;
            arquivoAtual     = file;
            historico.clear();
            undoButton.setEnabled(false);
            imageRaw.setIcon(new ImageIcon(originalImage));
            lblTransformedImage.setIcon(null);
            atualizarMetadados(file, img);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar a imagem.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Atualiza rótulo de metadados
    private void atualizarMetadados(File file, BufferedImage img) {
        String nome    = file.getName();
        int largura    = img.getWidth();
        int altura     = img.getHeight();
        long bytes     = file.length();
        String tamanho = (bytes >= 1_048_576)
                ? String.format("%.2f MB", bytes / 1_048_576.0)
                : String.format("%.1f KB", bytes / 1_024.0);

        lblMetadados.setText(String.format(
                "  \uD83D\uDCC4 %s   |   \uD83D\uDCD0 %d \u00D7 %d px   |   \uD83D\uDCBE %s",
                nome, largura, altura, tamanho));
    }

    // Abrir / Salvar imagem
    private void abrirImagem() {
        FileDialog fd = new FileDialog(this, "Selecione a imagem", FileDialog.LOAD);
        fd.setVisible(true);
        if (fd.getFile() != null) carregarArquivo(new File(fd.getDirectory() + fd.getFile()));
    }

    private void salvarImagem() {
        if (transformedImage == null) {
            JOptionPane.showMessageDialog(this,
                    "Nenhuma imagem transformada para salvar.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        FileDialog fd = new FileDialog(this, "Salvar imagem", FileDialog.SAVE);
        fd.setFile("*.png");
        fd.setVisible(true);
        String fileName = fd.getFile();
        String dir      = fd.getDirectory();
        if (fileName != null && dir != null) {
            if (!fileName.toLowerCase().endsWith(".png")) fileName += ".png";
            try {
                ImageIO.write(transformedImage, "png", new File(dir + fileName));
                JOptionPane.showMessageDialog(this,
                        "Imagem salva com sucesso.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao salvar a imagem.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Painel().setVisible(true));
    }
}