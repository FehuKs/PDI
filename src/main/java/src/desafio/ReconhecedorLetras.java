package src.desafio;

import src.core.Images;
import src.infra.CvScope;
import src.core.AnalysisResult;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import src.filtros.Grayscale;
import src.filtros.Limiarizacao;
import src.filtros.FiltrodaMediana;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ReconhecedorLetras {


    private static final int    NORM        = 40;
    private static final int    MARGEM      = 4;
    private static final double AREA_MIN    = 30.0;
    private static final int    LIMIAR_FIXO = 128;

    private final Grayscale     grayscale;
    private final Limiarizacao  limiarizacao;
    private final FiltrodaMediana filtroMediana;

    private static Map<Character, byte[]> templatesLetras;

    public ReconhecedorLetras(Grayscale g, Limiarizacao l, FiltrodaMediana f) {
        this.grayscale     = g;
        this.limiarizacao  = l;
        this.filtroMediana = f;
    }

    // ===================================================================
    // Método principal
    // ===================================================================
    public String reconhecer(BufferedImage image) { return evaluate(image).summary(); }

    public AnalysisResult evaluate(BufferedImage image) {
        Images.require(image);
        try (CvScope resources = CvScope.open()) {
            return reconhecerInternal(image);
        }
    }

    private AnalysisResult reconhecerInternal(BufferedImage image) {
        Mat bin = preprocessar(image);

        List<Rect> caixas = segmentar(bin, Imgproc.RETR_EXTERNAL);

        if (caixas.isEmpty()) return new AnalysisResult(AnalysisResult.Status.NOT_FOUND, "Nenhuma letra identificada", java.util.Map.of());

        caixas.sort(Comparator.comparingInt(r -> r.x));

        List<Character> letras = new ArrayList<>();
        for (Rect bb : caixas) {
            Mat glifo = CvScope.track(new Mat(bin, bb));
            Mat norm  = OpenCVUtils.normalizarGlifo(glifo, NORM, MARGEM);
            char letra = classificarLetra(norm);
            letras.add(letra);
        }

        if (letras.isEmpty()) return new AnalysisResult(AnalysisResult.Status.NOT_FOUND, "Nenhuma letra identificada", java.util.Map.of());

        StringBuilder sb = new StringBuilder();
        for (Character c : letras) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(c);
        }
        return new AnalysisResult(letras.contains('?') ? AnalysisResult.Status.INCONCLUSIVE : AnalysisResult.Status.IDENTIFIED,
                sb.toString(), java.util.Map.of("glyphs", String.valueOf(letras.size())));
    }

    // ===================================================================
    // preprocessar — Grayscale → Mediana → Limiarizacao(128) → invert
    // ===================================================================
    private Mat preprocessar(BufferedImage image) {
        BufferedImage cinza  = grayscale.aplicarGrayscale(image);
        BufferedImage suave  = filtroMediana.aplicarFiltroMediana(cinza);
        BufferedImage binImg = limiarizacao.aplicarLimiarizacao(suave, LIMIAR_FIXO);

        Mat binRaw = OpenCVUtils.toMatGray(binImg);
        Mat bin    = CvScope.track(new Mat());
        Core.bitwise_not(binRaw, bin);

        // Fallback Otsu
        double total = bin.rows() * (double) bin.cols();
        double razao = Core.countNonZero(bin) / total;
        if (razao < 0.002 || razao > 0.7) {
            Mat suaveMat = OpenCVUtils.toMatGray(suave);
            Mat binOtsu  = CvScope.track(new Mat());
            Imgproc.threshold(suaveMat, binOtsu, 0, 255,
                Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
            return binOtsu;
        }
        return bin;
    }

    // ===================================================================
    // segmentar — findContours com modo configurável
    // ===================================================================
    private List<Rect> segmentar(Mat bin, int modo) {
        List<MatOfPoint> contornos = new ArrayList<>();
        Imgproc.findContours(CvScope.track(bin.clone()), contornos, CvScope.track(new Mat()),
            modo, Imgproc.CHAIN_APPROX_SIMPLE);
        contornos.forEach(CvScope::track);
        List<Rect> caixas = new ArrayList<>();
        for (MatOfPoint c : contornos) {
            if (Imgproc.contourArea(c) >= AREA_MIN)
                caixas.add(Imgproc.boundingRect(c));
        }
        return caixas;
    }

    // ===================================================================
    // Templates A-Z
    // ===================================================================
    private static byte[] bytes(Mat matrix) {
        byte[] data = new byte[(int) matrix.total()];
        matrix.get(0, 0, data);
        return data;
    }

    private static synchronized Map<Character, byte[]> getTemplatesLetras() {
        if (templatesLetras == null) {
            Map<Character, byte[]> generated = new HashMap<>();
            for (char c = 'A'; c <= 'Z'; c++)
                generated.put(c, bytes(gerarTemplateLetra(c)));
            templatesLetras = java.util.Map.copyOf(generated);
        }
        return templatesLetras;
    }

    private static Mat gerarTemplateLetra(char letra) {
        int tam = 200;
        BufferedImage img = new BufferedImage(tam, tam, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, tam, tam);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("SansSerif", Font.BOLD, (int)(tam * 0.8)));
        FontMetrics fm = g2.getFontMetrics();
        String s = String.valueOf(letra);
        g2.drawString(s, (tam - fm.stringWidth(s)) / 2,
                         (tam - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
        Mat mat = OpenCVUtils.toMatGray(img);
        Mat bin = CvScope.track(new Mat());
        Imgproc.threshold(mat, bin, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
        return OpenCVUtils.normalizarGlifo(bin, NORM, MARGEM);
    }

    private static char classificarLetra(Mat glifo) {
        Map<Character, byte[]> tpls = getTemplatesLetras();
        char melhor = '?'; double melhorScore = -1;
        for (Map.Entry<Character, byte[]> e : tpls.entrySet()) {
            Mat template = new Mat(NORM, NORM, CvType.CV_8UC1);
            template.put(0, 0, e.getValue());
            double score;
            try { score = OpenCVUtils.comparar(glifo, template, NORM); } finally { template.release(); }
            if (score > melhorScore) { melhorScore = score; melhor = e.getKey(); }
        }
        return melhorScore >= 0.70 ? melhor : '?';
    }
}
