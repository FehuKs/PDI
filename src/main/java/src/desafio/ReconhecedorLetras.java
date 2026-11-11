package src.desafio;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import src.core.AnalysisResult;
import src.core.Images;
import src.filtros.FiltrodaMediana;
import src.filtros.Grayscale;
import src.filtros.Limiarizacao;
import src.infra.CvScope;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReconhecedorLetras {

    private static final int NORM = 40;
    private static final int MARGEM = 4;
    private static final double AREA_MIN = 30.0;
    private static final int LIMIAR_FIXO = 128;

    private final Grayscale grayscale;
    private final Limiarizacao limiarizacao;
    private final FiltrodaMediana filtroMediana;

    private static GlyphTemplates templatesLetras;

    public ReconhecedorLetras(Grayscale g, Limiarizacao l, FiltrodaMediana f) {
        this.grayscale = g;
        this.limiarizacao = l;
        this.filtroMediana = f;
    }

    // ===================================================================
    // Método principal
    // ===================================================================
    public String reconhecer(BufferedImage image) {
        return evaluate(image).summary();
    }

    public AnalysisResult evaluate(BufferedImage image) {
        Images.require(image);
        try (CvScope resources = CvScope.open()) {
            return reconhecerInternal(image);
        }
    }

    private AnalysisResult reconhecerInternal(BufferedImage image) {
        Mat bin = preprocessar(image);

        List<Rect> caixas = segmentar(bin, Imgproc.RETR_EXTERNAL);

        if (caixas.isEmpty())
            return new AnalysisResult(
                    AnalysisResult.Status.NOT_FOUND,
                    "Nenhuma letra identificada",
                    java.util.Map.of());

        caixas.sort(Comparator.comparingInt(r -> r.x));

        List<Character> letras = new ArrayList<>();
        for (Rect bb : caixas) {
            Mat glifo = CvScope.track(new Mat(bin, bb));
            Mat norm = OpenCVUtils.normalizarGlifo(glifo, NORM, MARGEM);
            char letra = classificarLetra(norm);
            letras.add(letra);
        }

        if (letras.isEmpty())
            return new AnalysisResult(
                    AnalysisResult.Status.NOT_FOUND,
                    "Nenhuma letra identificada",
                    java.util.Map.of());

        StringBuilder sb = new StringBuilder();
        for (Character c : letras) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(c);
        }
        return new AnalysisResult(
                letras.contains('?')
                        ? AnalysisResult.Status.INCONCLUSIVE
                        : AnalysisResult.Status.IDENTIFIED,
                sb.toString(),
                java.util.Map.of("glyphs", String.valueOf(letras.size())));
    }

    // ===================================================================
    // preprocessar — Grayscale → Mediana → Limiarizacao(128) → invert
    // ===================================================================
    private Mat preprocessar(BufferedImage image) {
        BufferedImage cinza = grayscale.aplicarGrayscale(image);
        BufferedImage suave = filtroMediana.aplicarFiltroMediana(cinza);
        BufferedImage binImg = limiarizacao.aplicarLimiarizacao(suave, LIMIAR_FIXO);

        Mat binRaw = OpenCVUtils.toMatGray(binImg);
        Mat bin = CvScope.track(new Mat());
        Core.bitwise_not(binRaw, bin);

        // Fallback Otsu
        double total = bin.rows() * (double) bin.cols();
        double razao = Core.countNonZero(bin) / total;
        if (razao < 0.002 || razao > 0.7) {
            Mat suaveMat = OpenCVUtils.toMatGray(suave);
            Mat binOtsu = CvScope.track(new Mat());
            Imgproc.threshold(
                    suaveMat, binOtsu, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
            return binOtsu;
        }
        return bin;
    }

    // ===================================================================
    // segmentar — findContours com modo configurável
    // ===================================================================
    private List<Rect> segmentar(Mat bin, int modo) {
        List<MatOfPoint> contornos = new ArrayList<>();
        Imgproc.findContours(
                CvScope.track(bin.clone()),
                contornos,
                CvScope.track(new Mat()),
                modo,
                Imgproc.CHAIN_APPROX_SIMPLE);
        contornos.forEach(CvScope::track);
        List<Rect> caixas = new ArrayList<>();
        for (MatOfPoint c : contornos) {
            if (Imgproc.contourArea(c) >= AREA_MIN) caixas.add(Imgproc.boundingRect(c));
        }
        return caixas;
    }

    // ===================================================================
    // Templates A-Z
    // ===================================================================
    private static synchronized GlyphTemplates getTemplatesLetras() {
        if (templatesLetras == null) templatesLetras = GlyphTemplates.letters(NORM, MARGEM);
        return templatesLetras;
    }

    private static char classificarLetra(Mat glifo) {
        // Solid convex shapes are not upright letters; retain narrow bars for the letter I.
        MatOfPoint foreground = CvScope.track(new MatOfPoint());
        Core.findNonZero(glifo, foreground);
        if (foreground.empty()) return '?';
        Rect bounds = Imgproc.boundingRect(foreground);
        if (bounds.width > bounds.height * 0.65) {
            MatOfInt indices = CvScope.track(new MatOfInt());
            Imgproc.convexHull(foreground, indices);
            Point[] points = foreground.toArray();
            Point[] hullPoints =
                    java.util.Arrays.stream(indices.toArray())
                            .mapToObj(i -> points[i])
                            .toArray(Point[]::new);
            MatOfPoint hull = CvScope.track(new MatOfPoint(hullPoints));
            double hullArea = Imgproc.contourArea(hull);
            if (hullArea > 0 && Core.countNonZero(glifo) / hullArea > 0.95) return '?';
        }
        return getTemplatesLetras().classify(glifo, 0.70);
    }
}
