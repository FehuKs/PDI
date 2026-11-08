package src.desafio;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import src.filtros.Grayscale;
import src.filtros.Limiarizacao;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalisadorBarras {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static final int    NORM          = 32;
    private static final int    MARGEM        = 6;
    private static final double AREA_MIN      = 15.0;
    private static final int    TOL_BASE      = 20;
    private static final int    LIMIAR_FIXO   = 128;
    private static final int    SATURACAO_MIN = 60;

    // Tolerância vertical para considerar um dígito "na mesma linha" do topo da barra
    private static final int TOL_Y = 30;

    @SuppressWarnings("unused")
    private final Grayscale grayscale;
    private final Limiarizacao limiarizacao;

    private static Map<Character, Mat> templatesDigitos;

    public AnalisadorBarras(Grayscale g, Limiarizacao l) {
        this.grayscale    = g;
        this.limiarizacao = l;
    }

    // Método principal

    public String analisar(BufferedImage image) {
        Mat bgr = toMat(image);
        Mat bin = binarizar(image, bgr);

        // Segmentação: todos os blobs
        List<MatOfPoint> contornos = new ArrayList<>();
        Imgproc.findContours(bin.clone(), contornos, new Mat(),
            Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        int imgW = bin.cols();
        int imgH = bin.rows();

        List<Rect> blobs = new ArrayList<>();
        for (MatOfPoint c : contornos) {
            if (Imgproc.contourArea(c) < AREA_MIN) continue;
            Rect r = Imgproc.boundingRect(c);

            // FILTRO 1: rejeita linhas horizontais (h < 5px) e verticais (w < 5px)
            if (r.height < 5 || r.width < 5) continue;

            // FILTRO 2: rejeita divisor vertical de janela — blob estreito que ocupa
            //           quase toda a altura da imagem (largura pequena, altura enorme)
            boolean estreito = r.width < imgW * 0.05;
            boolean altisso  = r.height > imgH * 0.80;
            if (estreito && altisso) continue;

            // FILTRO 3: razao de aspecto absurda — linha horizontal fina (grade)
            double ratio = (double) r.width / r.height;
            if (ratio > 30.0) continue;

            blobs.add(r);
        }

        if (blobs.isEmpty()) return "Nenhuma barra identificada";

        // Linha de base
        int base = 0;
        for (Rect r : blobs) {
            double ratio = (double) r.width / r.height;
            // ratio < 5.0 garante que não usaremos linhas de grade horizontais que escaparam,
            // mas permite a leitura de barras altas (ratio baixo) e dígitos.
            if (ratio < 5.0) {
                base = Math.max(base, r.y + r.height);
            }
        }

        // Separa barras (tocam a base com tamanho significativo) de dígitos
        List<Rect> barras  = new ArrayList<>();
        List<Rect> digitos = new ArrayList<>();
        for (Rect r : blobs) {
            boolean tocaBase  = r.y + r.height >= base - TOL_BASE;
            boolean alturaOk  = r.height > imgH * 0.05;
            boolean larguraOk = r.width  > imgW * 0.02;

            if (tocaBase && alturaOk && larguraOk) {
                barras.add(r);
            } else {
                // Todo o resto que passou nos filtros iniciais é salvo como dígito
                // (mesmo que toque o chão, como os números do eixo X ou o "0" do eixo Y)
                digitos.add(r);
            }
        }
        if (barras.isEmpty()) return "Nenhuma barra identificada";

        // ── MUDANÇA: separa dígitos da LEGENDA (esquerda das barras)
        //            dos dígitos de RÓTULO (acima/dentro das barras)
        int xMinBarra = Integer.MAX_VALUE;
        for (Rect b : barras) xMinBarra = Math.min(xMinBarra, b.x);

        List<Rect> digitosLegenda = new ArrayList<>(); // eixo Y (esquerda)
        List<Rect> digitosRotulo  = new ArrayList<>(); // rótulos sobre as barras
        for (Rect d : digitos) {
            // Usa o CENTRO X do dígito como critério: dígitos da legenda têm
            // seu centro bem à esquerda da borda da primeira barra.
            // Tolerância de -10px evita capturar rótulos que começam próximos à barra.
            int centroXDigito = d.x + d.width / 2;
            if (centroXDigito < xMinBarra - 10) digitosLegenda.add(d);
            else                                digitosRotulo.add(d);
        }

        // ── Identifica barra mais alta e mais baixa por altura em pixels
        barras.sort(Comparator.comparingInt(r -> r.x));

        Rect barraMaior = barras.get(0);
        Rect barraMenor = barras.get(0);
        for (Rect b : barras) {
            if ((base - b.y) > (base - barraMaior.y)) barraMaior = b;
            if ((base - b.y) < (base - barraMenor.y)) barraMenor = b;
        }

        // ── Lê o valor de cada barra: primeiro tenta rótulo acima da barra,
        //    depois faz fallback para a legenda do eixo Y.
        int valorMaior = lerRotuloDaBarra(bin, barraMaior, digitosRotulo);
        if (valorMaior == 0) {
            valorMaior = lerLegendaNoTopoY(bin, barraMaior.y, digitosLegenda, base);
        }

        // Se todas as barras têm a mesma altura, barraMenor == barraMaior.
        // Nesse caso, reutiliza o valorMaior já calculado para o menor.
        int valorMenor;
        if (barraMenor.y == barraMaior.y) {
            valorMenor = valorMaior;
        } else {
            valorMenor = lerRotuloDaBarra(bin, barraMenor, digitosRotulo);
            if (valorMenor == 0) {
                valorMenor = lerLegendaNoTopoY(bin, barraMenor.y, digitosLegenda, base);
            }
        }

        return "Maior = " + valorMaior + " | Menor = " + valorMenor;
    }

    // lerRotuloDaBarra
    // Verifica os dígitos posicionados diretamente sobre a largura da barra
    
    private int lerRotuloDaBarra(Mat bin, Rect barra, List<Rect> digitosRotulo) {
        List<Rect> digitosDaBarra = new ArrayList<>();
        int barraCentroX = barra.x + barra.width / 2;

        for (Rect d : digitosRotulo) {
            int cx = d.x + d.width / 2;
            // Se o centro horizontal do dígito estiver contido na largura da barra
            // (A tolerância de 20 pixels acomoda números mais largos que barras finas)
            if (Math.abs(cx - barraCentroX) <= (barra.width / 2) + 20) {
                digitosDaBarra.add(d);
            }
        }

        if (digitosDaBarra.isEmpty()) return 0;

        // Ordena os dígitos da esquerda para a direita para compor números com 2+ dígitos
        digitosDaBarra.sort(Comparator.comparingInt(r -> r.x));
        return reconhecerNumero(bin, digitosDaBarra);
    }

    // ===================================================================
    // lerLegendaNoTopoY
    //
    // Recebe o Y do topo de uma barra e encontra o número da legenda
    // do eixo Y que está na mesma linha horizontal.
    //
    // 1. Busca direta: dígitos da legenda com centroY ± TOL_Y do topoY.
    // 2. Se não encontrar: interpola entre os dois marcadores vizinhos
    //    (um acima e um abaixo do topoY).
    // ===================================================================
    private int lerLegendaNoTopoY(Mat bin, int topoY, List<Rect> digitosLegenda, int baseY) {

        // Passo 1 — busca direta na linha do topo da barra
        List<Rect> naLinha = new ArrayList<>();
        for (Rect d : digitosLegenda) {
            int cy = d.y + d.height / 2;
            if (Math.abs(cy - topoY) <= TOL_Y) naLinha.add(d);
        }

        if (!naLinha.isEmpty()) {
            naLinha.sort(Comparator.comparingInt(r -> r.x));
            return reconhecerNumero(bin, naLinha);
        }

        // Passo 2 — interpola entre os dois marcadores mais próximos
        // Agrupa dígitos da legenda por linha Y (dígitos do mesmo número)
        List<int[]> marcadores = agruparMarcadores(bin, digitosLegenda);

        // Adiciona âncora base (valor 0 na linha de base)
        marcadores.add(new int[]{0, baseY});
        marcadores.sort(Comparator.comparingInt(p -> p[1])); // topo→base

        // Encontra os dois marcadores que cercam o topoY
        int[] acima  = null; // posY ≤ topoY (valor maior)
        int[] abaixo = null; // posY > topoY (valor menor)
        for (int[] m : marcadores) {
            if (m[1] <= topoY) acima  = m;
            else if (abaixo == null) abaixo = m;
        }

        if (acima == null && abaixo != null) return abaixo[0];
        if (abaixo == null && acima != null) return acima[0];
        if (acima == null)                   return 0;

        // Interpolação linear
        int posA = acima[1],  valA = acima[0];
        int posB = abaixo[1], valB = abaixo[0];
        if (posB == posA) return valA;
        double frac = (double)(topoY - posA) / (posB - posA);
        return (int) Math.round(valA + frac * (valB - valA));
    }

    // agruparMarcadores
    //
    // Agrupa os dígitos da legenda por linha Y e reconhece o número
    // de cada grupo. Retorna lista de int[]{valor, posYcentro}.

    private List<int[]> agruparMarcadores(Mat bin, List<Rect> digitosLegenda) {
        if (digitosLegenda.isEmpty()) return new ArrayList<>();

        List<Rect> ordenados = new ArrayList<>(digitosLegenda);
        ordenados.sort(Comparator.comparingInt(r -> r.y));

        List<List<Rect>> linhas = new ArrayList<>();
        List<Rect> atual = new ArrayList<>();
        atual.add(ordenados.get(0));

        for (int i = 1; i < ordenados.size(); i++) {
            int cyPrev = ordenados.get(i-1).y + ordenados.get(i-1).height / 2;
            int cyCur  = ordenados.get(i).y   + ordenados.get(i).height   / 2;
            if (Math.abs(cyCur - cyPrev) < 10) {
                atual.add(ordenados.get(i));
            } else {
                linhas.add(atual);
                atual = new ArrayList<>();
                atual.add(ordenados.get(i));
            }
        }
        linhas.add(atual);

        List<int[]> marcadores = new ArrayList<>();
        for (List<Rect> linha : linhas) {
            linha.sort(Comparator.comparingInt(r -> r.x));
            int valor = reconhecerNumero(bin, linha);
            int somaY = 0;
            for (Rect d : linha) somaY += d.y + d.height / 2;
            int centroY = somaY / linha.size();
            marcadores.add(new int[]{valor, centroY});
        }
        return marcadores;
    }

    // reconhecerNumero — reconhece dígitos de uma lista de blobs (esq→dir)

    private int reconhecerNumero(Mat bin, List<Rect> digitos) {
        StringBuilder numero = new StringBuilder();
        for (Rect d : digitos) {
            int x = Math.max(0, d.x);
            int y = Math.max(0, d.y);
            int w = Math.min(bin.cols() - x, d.width);
            int h = Math.min(bin.rows() - y, d.height);
            if (w <= 0 || h <= 0) continue;
            Mat glifo = new Mat(bin, new Rect(x, y, w, h));
            Mat norm  = normalizarGlifo(glifo);
            char dig  = classificarDigito(norm);
            if (dig != '?') numero.append(dig);
        }
        if (numero.length() == 0) return 0;
        try { return Integer.parseInt(numero.toString()); }
        catch (NumberFormatException e) { return 0; }
    }

    

    // Binarização combinada — idêntica ao original

    private Mat binarizar(BufferedImage image, Mat bgr) {
        BufferedImage cinza  = grayscale.aplicarGrayscale(image);
        BufferedImage binImg = limiarizacao.aplicarLimiarizacao(cinza, LIMIAR_FIXO);
        Mat binTextoRaw = toMatGray(binImg);
        Mat binTexto    = new Mat();
        Core.bitwise_not(binTextoRaw, binTexto);

        Mat hsv = new Mat();
        Imgproc.cvtColor(bgr, hsv, Imgproc.COLOR_BGR2HSV);
        List<Mat> canais = new ArrayList<>();
        Core.split(hsv, canais);
        Mat binSat = new Mat();
        Imgproc.threshold(canais.get(1), binSat, SATURACAO_MIN, 255, Imgproc.THRESH_BINARY);

        Mat bin = new Mat();
        Core.bitwise_or(binTexto, binSat, bin);
        return bin;
    }

    // normalizarGlifo — idêntico ao original

    private static Mat normalizarGlifo(Mat bin) {
        Mat naoZero = new Mat();
        Core.findNonZero(bin, naoZero);
        if (naoZero.empty()) return Mat.zeros(NORM, NORM, CvType.CV_8UC1);
        Rect bb   = Imgproc.boundingRect(naoZero);
        Mat  crop = new Mat(bin, bb);
        int    alvo   = NORM - 2 * MARGEM;
        double escala = Math.min((double) alvo / crop.cols(), (double) alvo / crop.rows());
        int    novaW  = Math.max(1, (int) Math.round(crop.cols() * escala));
        int    novaH  = Math.max(1, (int) Math.round(crop.rows() * escala));
        Mat resized = new Mat();
        Imgproc.resize(crop, resized, new Size(novaW, novaH));
        Mat canvas = Mat.zeros(NORM, NORM, CvType.CV_8UC1);
        resized.copyTo(canvas.submat(new Rect((NORM-novaW)/2, (NORM-novaH)/2, novaW, novaH)));
        return canvas;
    }

    // Templates 0-9 — idênticos ao original

    private static synchronized Map<Character, Mat> getTemplatesDigitos() {
        if (templatesDigitos == null) {
            templatesDigitos = new HashMap<>();
            for (char d = '0'; d <= '9'; d++)
                templatesDigitos.put(d, gerarTemplateDigito(d));
        }
        return templatesDigitos;
    }

    private static Mat gerarTemplateDigito(char digito) {
        int tam = 100;
        BufferedImage img = new BufferedImage(tam, tam, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(Color.WHITE); g2.fillRect(0, 0, tam, tam);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("SansSerif", Font.BOLD, (int)(tam * 0.8)));
        FontMetrics fm = g2.getFontMetrics();
        String s = String.valueOf(digito);
        g2.drawString(s, (tam - fm.stringWidth(s)) / 2,
                         (tam - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
        Mat mat = toMatGray(img);
        Mat bin = new Mat();
        Imgproc.threshold(mat, bin, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
        return normalizarGlifo(bin);
    }

    private static char classificarDigito(Mat glifo) {
        Map<Character, Mat> tpls = getTemplatesDigitos();
        char melhor = '?'; double melhorScore = -1;
        for (Map.Entry<Character, Mat> e : tpls.entrySet()) {
            double score = comparar(glifo, e.getValue());
            if (score > melhorScore) { melhorScore = score; melhor = e.getKey(); }
        }
        return melhor;
    }

    private static double comparar(Mat a, Mat b) {
        Mat xor = new Mat();
        Core.bitwise_xor(a, b, xor);
        return 1.0 - (double) Core.countNonZero(xor) / (NORM * NORM);
    }

    private static Mat toMat(BufferedImage img) {
        BufferedImage bgr = new BufferedImage(
            img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        bgr.getGraphics().drawImage(img, 0, 0, null);
        byte[] data = ((DataBufferByte) bgr.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(bgr.getHeight(), bgr.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);
        return mat;
    }

    private static Mat toMatGray(BufferedImage img) {
        BufferedImage g = new BufferedImage(
            img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        g.getGraphics().drawImage(img, 0, 0, null);
        byte[] data = ((DataBufferByte) g.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(g.getHeight(), g.getWidth(), CvType.CV_8UC1);
        mat.put(0, 0, data);
        return mat;
    }
}