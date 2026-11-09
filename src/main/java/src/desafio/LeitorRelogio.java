package src.desafio;

import src.core.Images;
import src.infra.CvScope;
import src.infra.OpenCvRuntime;
import src.core.AnalysisResult;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import src.filtros.Grayscale;
import src.filtros.Limiarizacao;
import src.afinamento.ZhangSuen;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class LeitorRelogio {

    // ===================================================================
    // Tipos internos
    // ===================================================================

    public static class Ponto {
        public final int x, y;
        public Ponto(int x, int y) { this.x = x; this.y = y; }
    }

    public static class Linha {
        public final int    x1, y1, x2, y2;
        public final double comprimento;

        public Linha(int x1, int y1, int x2, int y2) {
            this.x1 = x1; this.y1 = y1;
            this.x2 = x2; this.y2 = y2;
            this.comprimento = Math.hypot(x2 - x1, y2 - y1);
        }

        /**
         * Ângulo do ponteiro em graus no referencial de relógio.
         *   12h = 0°, 3h = 90°, 6h = 180°, 9h = 270°
         *
         * - Usa o extremo mais DISTANTE do centro como ponta (fix bidirecional).
         * - Inverte dy para compensar o eixo Y invertido da imagem.
         * - Converte: anguloRelogio = (90° − atan2_graus + 360°) % 360°
         */
        public double angulo(Ponto c) {
            double d1 = Math.hypot(x1 - c.x, y1 - c.y);
            double d2 = Math.hypot(x2 - c.x, y2 - c.y);
            int px = (d1 >= d2) ? x1 : x2;
            int py = (d1 >= d2) ? y1 : y2;
            double dx =  (px - c.x);
            double dy = -(py - c.y); // inverte Y
            double anguloTrig    = Math.toDegrees(Math.atan2(dy, dx));
            double anguloRelogio = (90.0 - anguloTrig + 360.0) % 360.0;
            return anguloRelogio;
        }
    }

    // ===================================================================
    // Dependências — usadas ativamente no pipeline
    // ===================================================================
    private final Grayscale    grayscale;
    private final Limiarizacao limiarizacao;
    private final ZhangSuen    zhangSuen;

    public LeitorRelogio(Grayscale g, Limiarizacao l, ZhangSuen z) {
        this.grayscale    = g;
        this.limiarizacao = l;
        this.zhangSuen    = z;
    }

    // ===================================================================
    // Método de entrada
    // ===================================================================
    public String lerHorario(BufferedImage image) { return evaluate(image).summary(); }

    public AnalysisResult evaluate(BufferedImage image) {
        Images.require(image);
        try (CvScope resources = CvScope.open()) {
            return lerHorarioInternal(image);
        }
    }

    private AnalysisResult lerHorarioInternal(BufferedImage image) {
        OpenCvRuntime.ensureLoaded();

        Ponto centro     = findCenter(image);
        List<Linha> maos = detectHands(image);

        if (maos.isEmpty()) return new AnalysisResult(AnalysisResult.Status.INCONCLUSIVE, "??:??", java.util.Map.of());
        if (maos.size() == 1) {
            double angulo  = maos.get(0).angulo(centro);
            int minutos    = (int) Math.round(angulo / 6.0) % 60;
            if (minutos < 0) minutos += 60;
            return new AnalysisResult(AnalysisResult.Status.INCONCLUSIVE, String.format("??:%02d", minutos), java.util.Map.of("minute", String.valueOf(minutos)));
        }

        String time = calculateTime(getHourHand(maos), getMinuteHand(maos), centro);
        return new AnalysisResult(AnalysisResult.Status.IDENTIFIED, time, java.util.Map.of("time", time));
    }

    // ===================================================================
    // preprocessar — Grayscale → Limiarização → ZhangSuen
    // ===================================================================
    private BufferedImage preprocessar(BufferedImage image) {
        BufferedImage cinza = grayscale.aplicarGrayscale(image);
        BufferedImage bin   = limiarizacao.aplicarLimiarizacao(cinza, 128);
        BufferedImage fino  = zhangSuen.aplicarThinning(bin);
        return fino;
    }

    // ===================================================================
    // findCenter — HoughCircles na imagem ORIGINAL (não no thinning)
    // ===================================================================
    public Ponto findCenter(BufferedImage img) {
        OpenCvRuntime.ensureLoaded();
        Mat mat  = OpenCVUtils.toMat(img);
        Mat gray = CvScope.track(new Mat());
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray, gray, new Size(9, 9), 2.0);

        Mat circles = CvScope.track(new Mat());

        // Tentativa 1: parâmetros normais
        Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT,
            1, gray.rows() / 3.0, 100, 30,
            gray.rows() / 5, (int)(gray.rows() * 0.6));
        if (circles.cols() > 0) {
            double[] c = circles.get(0, 0);
            return new Ponto((int) Math.round(c[0]), (int) Math.round(c[1]));
        }

        // Tentativa 2: parâmetros permissivos (relógio parcialmente visível)
        Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT,
            1, gray.rows() / 4.0, 80, 20,
            gray.rows() / 8, (int)(gray.rows() * 0.8));
        if (circles.cols() > 0) {
            double[] c = circles.get(0, 0);
            return new Ponto((int) Math.round(c[0]), (int) Math.round(c[1]));
        }

        // Tentativa 3: centróide dos pixels escuros
        Mat bin = CvScope.track(new Mat());
        Imgproc.threshold(gray, bin, 0, 255,
            Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
        Moments mom = Imgproc.moments(bin);
        if (mom.m00 > 0) {
            int cx = (int)(mom.m10 / mom.m00);
            int cy = (int)(mom.m01 / mom.m00);
            int margem = mat.cols() / 6;
            if (cx < margem) cx = 0;
            else if (cx > mat.cols() - margem) cx = mat.cols();
            return new Ponto(cx, cy);
        }

        return new Ponto(mat.cols() / 2, mat.rows() / 2);
    }

    // ===================================================================
    // detectHands — pipeline: preprocessar() → Canny → HoughLinesP
    // ===================================================================
    public List<Linha> detectHands(BufferedImage img) {
        OpenCvRuntime.ensureLoaded();
        Ponto centro  = findCenter(img);
        int   dim     = Math.min(img.getWidth(), img.getHeight());
        double tol    = dim * 0.15;

        // ----------------------------------------------------------------
        // CAMINHO PRINCIPAL: Grayscale → Limiarização → ZhangSuen → Canny → Hough
        // ----------------------------------------------------------------
        BufferedImage fino   = preprocessar(img);
        Mat           matFino = OpenCVUtils.toMatGray(fino);

        Mat edges = CvScope.track(new Mat());
        Imgproc.Canny(matFino, edges, 10, 50);

        int comprMin = dim / 6;
        Mat linesMat = CvScope.track(new Mat());
        Imgproc.HoughLinesP(edges, linesMat,
            1, Math.PI / 180, 20, comprMin, 10);

        List<Linha> candidatos = filtrarPeloCentro(linesMat, centro, tol);
        List<Linha> resultado  = agrupar(candidatos, centro);
        if (resultado.size() >= 2) return resultado;
        List<Linha> melhorParcial = resultado;

        // ----------------------------------------------------------------
        // FALLBACK: imagem original com tentativas progressivas de Canny
        // ----------------------------------------------------------------
        int[][] tentativas = {
            {50, 150, 40, 6},
            {30, 100, 20, 8},
            {15,  60, 10, 12}
        };

        for (int[] p : tentativas) {
            Mat mat  = OpenCVUtils.toMat(img);
            Mat gray = CvScope.track(new Mat());
            Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);
            Imgproc.GaussianBlur(gray, gray, new Size(3, 3), 1.0);

            Mat edgesFb = CvScope.track(new Mat());
            Imgproc.Canny(gray, edgesFb, p[0], p[1]);

            Mat mask = CvScope.track(Mat.zeros(edgesFb.size(), CvType.CV_8UC1));
            Imgproc.circle(mask,
                new org.opencv.core.Point(centro.x, centro.y),
                dim / 2 - 5, new Scalar(255), -1);
            Core.bitwise_and(edgesFb, mask, edgesFb);

            Mat lmFb = CvScope.track(new Mat());
            Imgproc.HoughLinesP(edgesFb, lmFb,
                1, Math.PI / 180, p[2], dim / p[3], 15);

            List<Linha> cands = filtrarPeloCentro(lmFb, centro, tol);
            List<Linha> res   = agrupar(cands, centro);
            if (res.size() >= 2) return res;
            if (res.size() > melhorParcial.size()) melhorParcial = res;
        }

        return melhorParcial;
    }

    // ===================================================================
    // getHourHand — ponteiro mais curto = horas
    // ===================================================================
    public Linha getHourHand(List<Linha> hands) {
        if (hands == null || hands.isEmpty()) return null;
        Linha h = hands.get(0);
        for (Linha l : hands) if (l.comprimento < h.comprimento) h = l;
        return h;
    }

    // ===================================================================
    // getMinuteHand — ponteiro mais longo = minutos
    // ===================================================================
    public Linha getMinuteHand(List<Linha> hands) {
        if (hands == null || hands.isEmpty()) return null;
        Linha h = hands.get(0);
        for (Linha l : hands) if (l.comprimento > h.comprimento) h = l;
        return h;
    }

    // ===================================================================
    // calculateTime — ângulos → "HH:MM"
    // ===================================================================
    public String calculateTime(Linha hourHand, Linha minuteHand, Ponto center) {
        if (hourHand == null || minuteHand == null) return "??:??";

        double angMin  = minuteHand.angulo(center);
        double angHora = hourHand.angulo(center);

        int minutos = (int) Math.round(angMin / 6.0) % 60;
        if (minutos < 0) minutos += 60;

        double angHoraCorrig = ((angHora - minutos * 0.5) % 360.0 + 360.0) % 360.0;
        int hora = (int) Math.round(angHoraCorrig / 30.0) % 12;
        if (hora == 0) hora = 12;

        return String.format("%02d:%02d", hora, minutos);
    }

    // ===================================================================
    // Helpers privados
    // ===================================================================

    private List<Linha> filtrarPeloCentro(Mat linesMat, Ponto centro, double tol) {
        List<Linha> lista = new ArrayList<>();
        for (int i = 0; i < linesMat.rows(); i++) {
            double[] s = linesMat.get(i, 0);
            Linha l = new Linha((int)s[0], (int)s[1], (int)s[2], (int)s[3]);
            if (distPontoReta(l, centro) <= tol) {
                double d1 = Math.hypot(l.x1 - centro.x, l.y1 - centro.y);
                double d2 = Math.hypot(l.x2 - centro.x, l.y2 - centro.y);
                double dot = (l.x1 - centro.x) * (double)(l.x2 - centro.x)
                        + (l.y1 - centro.y) * (double)(l.y2 - centro.y);
                // Hough can return one segment spanning two collinear, opposite hands.
                if (dot < 0 && Math.min(d1, d2) > Math.max(5, tol * 0.5)) {
                    lista.add(new Linha(centro.x, centro.y, l.x1, l.y1));
                    lista.add(new Linha(centro.x, centro.y, l.x2, l.y2));
                } else lista.add(l);
            }
        }
        return lista;
    }

    private double distPontoReta(Linha l, Ponto p) {
        double dx  = l.x2 - l.x1;
        double dy  = l.y2 - l.y1;
        double len = Math.hypot(dx, dy);
        if (len < 1e-9) return Math.hypot(p.x - l.x1, p.y - l.y1);
        return Math.abs(dy * p.x - dx * p.y
               + l.x2 * (double)l.y1 - l.y2 * (double)l.x1) / len;
    }

    private List<Linha> agrupar(List<Linha> linhas, Ponto centro) {
        boolean[]   usada = new boolean[linhas.size()];
        List<Linha> res   = new ArrayList<>();
        for (int i = 0; i < linhas.size(); i++) {
            if (usada[i]) continue;
            Linha  melhor = linhas.get(i);
            double angI   = melhor.angulo(centro);
            for (int j = i + 1; j < linhas.size(); j++) {
                if (usada[j]) continue;
                double angJ   = linhas.get(j).angulo(centro);
                double diff   = Math.abs(angI - angJ);
                if (diff > 180) diff = 360 - diff;
                if (diff < 15) {
                    usada[j] = true;
                    if (linhas.get(j).comprimento > melhor.comprimento)
                        melhor = linhas.get(j);
                }
            }
            res.add(melhor);
        }
        return res;
    }
}
