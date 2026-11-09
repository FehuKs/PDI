package src.desafio;

import src.core.Images;
import src.infra.CvScope;
import src.core.AnalysisResult;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import src.filtros.Grayscale;
import src.filtros.Limiarizacao;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;


public class IdentificadorPlacas {
    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(IdentificadorPlacas.class.getName());


    private static final double AREA_MIN = 1500.0;

    public IdentificadorPlacas() {}

    /** Compatibility with the original constructor; this pipeline operates directly in HSV. */
    public IdentificadorPlacas(Grayscale grayscale, Limiarizacao limiarizacao) {
        java.util.Objects.requireNonNull(grayscale);
        java.util.Objects.requireNonNull(limiarizacao);
    }

    public String identificar(BufferedImage image) { return evaluate(image).summary(); }

    public AnalysisResult evaluate(BufferedImage image) {
        Images.require(image);
        try (CvScope resources = CvScope.open()) {
            return identificarInternal(image);
        }
    }

    private AnalysisResult identificarInternal(BufferedImage image) {
        Mat mat = OpenCVUtils.toMat(image);
        List<String> encontradas = recognize(mat);
        if (encontradas.isEmpty()) return new AnalysisResult(AnalysisResult.Status.NOT_FOUND, "Nenhuma placa identificada", java.util.Map.of());
        return new AnalysisResult(encontradas.contains("Placa desconhecida") ? AnalysisResult.Status.INCONCLUSIVE : AnalysisResult.Status.IDENTIFIED,
                String.join(", ", encontradas), java.util.Map.of("candidates", String.valueOf(encontradas.size())));
    }

    public List<MatOfPoint> detectSigns(Mat image) {
        Mat hsv = CvScope.track(new Mat());
        Imgproc.cvtColor(image, hsv, Imgproc.COLOR_BGR2HSV);

        Mat mv1 = CvScope.track(new Mat()), mv2 = CvScope.track(new Mat()), maskVerm = CvScope.track(new Mat());
        Core.inRange(hsv, new Scalar(0,   70, 70), new Scalar(10,  255, 255), mv1);
        Core.inRange(hsv, new Scalar(160, 70, 70), new Scalar(179, 255, 255), mv2);
        Core.bitwise_or(mv1, mv2, maskVerm);

        Mat kernel = CvScope.track(Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(7, 7)));
        Imgproc.morphologyEx(maskVerm, maskVerm, Imgproc.MORPH_CLOSE, kernel);
        Imgproc.morphologyEx(maskVerm, maskVerm, Imgproc.MORPH_OPEN,  kernel);

        List<MatOfPoint> contornos = new ArrayList<>();
        Imgproc.findContours(CvScope.track(maskVerm.clone()), contornos, CvScope.track(new Mat()),
            Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        contornos.forEach(CvScope::track);

        List<MatOfPoint> validos = new ArrayList<>();
        for (MatOfPoint c : contornos)
            if (Imgproc.contourArea(c) >= AREA_MIN) validos.add(c);

        return validos;
    }

    public String classifySign(Mat sign) {
        Mat hsv = CvScope.track(new Mat());
        Imgproc.cvtColor(sign, hsv, Imgproc.COLOR_BGR2HSV);

        // ---------------------------------------------------------------
        // Passo 0: Razão de preenchimento vermelho no crop
        // ---------------------------------------------------------------
        double totalPixels = sign.rows() * sign.cols();
        double pixelsVerm  = contarPixelsVermelho(hsv);
        double razaoVerm   = pixelsVerm / totalPixels;

        LOG.fine(String.format("[classifySign] recorte=%dx%d  razaoVerm=%.3f%n",
            sign.cols(), sign.rows(), razaoVerm));

        if (razaoVerm > 0.55) {
            LOG.fine("[classifySign] → PARE (preenchimento vermelho alto)");
            return "Pare";
        }

        // ---------------------------------------------------------------
        // Passo 1: Diagonal vermelha por proporção de linhas
        // ---------------------------------------------------------------
        boolean temDiagonal = temDiagonalPorProporcao(sign, hsv);
        LOG.fine("[classifySign] temDiagonal=" + temDiagonal);

        if (!temDiagonal) {
            LOG.fine("[classifySign] → VELOCIDADE MAXIMA");
            return "Velocidade maxima";
        }

        // ---------------------------------------------------------------
        // Passo 2: Seta interna
        // ---------------------------------------------------------------
        boolean temSeta = temSetaInterna(sign, hsv);
        LOG.fine("[classifySign] temSeta=" + temSeta);

        if (temSeta) {
            LOG.fine("[classifySign] → SENTIDO PROIBIDO");
            return "Sentido proibido";
        }
        LOG.fine("[classifySign] → PROIBIDO ESTACIONAR");
        return "Proibido estacionar";
    }

    public List<String> recognize(Mat image) {
        List<MatOfPoint> placas = detectSigns(image);
        List<String> resultado  = new ArrayList<>();
        for (MatOfPoint placa : placas) {
            Rect bb = Imgproc.boundingRect(placa);
            int x = Math.max(0, bb.x);
            int y = Math.max(0, bb.y);
            int w = Math.min(image.cols() - x, bb.width);
            int h = Math.min(image.rows() - y, bb.height);
            if (w <= 0 || h <= 0) continue;
            MatOfPoint2f curve = CvScope.track(new MatOfPoint2f(placa.toArray()));
            MatOfPoint2f polygon = CvScope.track(new MatOfPoint2f());
            Imgproc.approxPolyDP(curve, polygon, 0.025 * Imgproc.arcLength(curve, true), true);
            long vertices = polygon.total();
            double fill = Imgproc.contourArea(placa) / (w * (double) h);
            double aspect = w / (double) h;
            // Filled rectangles are not stop signs; a candidate must resemble an octagon or circle.
            if (vertices < 6 || aspect < 0.75 || aspect > 1.25 || fill < 0.5) {
                resultado.add("Placa desconhecida");
            } else {
                resultado.add(classifySign(CvScope.track(new Mat(image, new Rect(x, y, w, h)))));
            }
        }
        return resultado;
    }

    // ===================================================================
    // Helpers
    // ===================================================================

    private double contarPixelsVermelho(Mat hsv) {
        Mat m1 = CvScope.track(new Mat()), m2 = CvScope.track(new Mat()), combined = CvScope.track(new Mat());
        Core.inRange(hsv, new Scalar(0,   50, 50), new Scalar(10,  255, 255), m1);
        Core.inRange(hsv, new Scalar(160, 50, 50), new Scalar(179, 255, 255), m2);
        Core.bitwise_or(m1, m2, combined);
        return Core.countNonZero(combined);
    }

    private boolean temDiagonalPorProporcao(Mat sign, Mat hsv) {
        Mat m1 = CvScope.track(new Mat()), m2 = CvScope.track(new Mat()), maskVerm = CvScope.track(new Mat());
        Core.inRange(hsv, new Scalar(0,   50, 50), new Scalar(10,  255, 255), m1);
        Core.inRange(hsv, new Scalar(160, 50, 50), new Scalar(179, 255, 255), m2);
        Core.bitwise_or(m1, m2, maskVerm);

        int    menorDim = Math.min(sign.rows(), sign.cols());
        int    minVotos = Math.max(8, menorDim / 20);
        double minLen   = menorDim / 8.0;

        Mat lines = CvScope.track(new Mat());
        Imgproc.HoughLinesP(maskVerm, lines, 1, Math.PI / 180, minVotos, minLen, 25.0);

        if (lines.rows() == 0) return false;

        int total    = lines.rows();
        int diagonal = 0;
        for (int i = 0; i < total; i++) {
            double[] s   = lines.get(i, 0);
            double   ang = Math.abs(Math.toDegrees(
                Math.atan2(s[3] - s[1], s[2] - s[0])));
            if ((ang >= 25 && ang <= 65) || (ang >= 115 && ang <= 155))
                diagonal++;
        }

        double prop = (double) diagonal / total;
        LOG.fine(String.format("[temDiagonalPorProporcao] total=%d diagonal=%d prop=%.2f%n",
            total, diagonal, prop));

        return prop > 0.28 && diagonal >= 3;
    }

    private boolean temSetaInterna(Mat sign, Mat hsv) {
        Mat m1 = CvScope.track(new Mat()), m2 = CvScope.track(new Mat()), maskVerm = CvScope.track(new Mat()), maskNaoVerm = CvScope.track(new Mat());
        Core.inRange(hsv, new Scalar(0,   40, 40), new Scalar(10,  255, 255), m1);
        Core.inRange(hsv, new Scalar(160, 40, 40), new Scalar(179, 255, 255), m2);
        Core.bitwise_or(m1, m2, maskVerm);
        Core.bitwise_not(maskVerm, maskNaoVerm);

        Mat gray = CvScope.track(new Mat()), bin = CvScope.track(new Mat());
        Imgproc.cvtColor(sign, gray, Imgproc.COLOR_BGR2GRAY);
        Core.bitwise_and(gray, gray, gray, maskNaoVerm);
        Imgproc.threshold(gray, bin, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);

        int margem  = sign.cols() / 6;
        Mat binCrop = CvScope.track(new Mat(bin, new Rect(margem, margem,
            bin.cols() - 2 * margem, bin.rows() - 2 * margem)));

        int    menorDim = Math.min(binCrop.rows(), binCrop.cols());
        int    minVotos = Math.max(10, menorDim / 10);
        double minLen   = menorDim / 5.0;

        Mat lines = CvScope.track(new Mat());
        Imgproc.HoughLinesP(binCrop, lines, 1, Math.PI / 180, minVotos, minLen, 15.0);

        int horizontais = 0;
        int verticais   = 0;

        for (int i = 0; i < lines.rows(); i++) {
            double[] s   = lines.get(i, 0);
            double   ang = Math.abs(Math.toDegrees(
                Math.atan2(s[3] - s[1], s[2] - s[0])));
            if (ang <= 20 || ang >= 160) horizontais++;
            if (ang >= 70 && ang <= 110) verticais++;
        }

        LOG.fine(String.format("[temSetaInterna] linhas total=%d  horiz=%d  vert=%d%n",
            lines.rows(), horizontais, verticais));

        boolean ehSeta = horizontais < 5;
        LOG.fine("[temSetaInterna] ehSeta=" + ehSeta);
        return ehSeta;
    }
}
