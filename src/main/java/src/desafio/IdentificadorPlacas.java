package src.desafio;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import src.filtros.Grayscale;
import src.filtros.Limiarizacao;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;


public class IdentificadorPlacas {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static final double AREA_MIN = 1500.0;

    @SuppressWarnings("unused") private final Grayscale    grayscale;
    @SuppressWarnings("unused") private final Limiarizacao limiarizacao;

    public IdentificadorPlacas(Grayscale g, Limiarizacao l) {
        this.grayscale    = g;
        this.limiarizacao = l;
    }

    public String identificar(BufferedImage image) {
        Mat mat = OpenCVUtils.toMat(image);
        List<String> encontradas = recognize(mat);
        if (encontradas.isEmpty()) return "Nenhuma placa identificada";
        return String.join(", ", encontradas);
    }

    public List<MatOfPoint> detectSigns(Mat image) {
        Mat hsv = new Mat();
        Imgproc.cvtColor(image, hsv, Imgproc.COLOR_BGR2HSV);

        Mat mv1 = new Mat(), mv2 = new Mat(), maskVerm = new Mat();
        Core.inRange(hsv, new Scalar(0,   70, 70), new Scalar(10,  255, 255), mv1);
        Core.inRange(hsv, new Scalar(160, 70, 70), new Scalar(179, 255, 255), mv2);
        Core.bitwise_or(mv1, mv2, maskVerm);

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(7, 7));
        Imgproc.morphologyEx(maskVerm, maskVerm, Imgproc.MORPH_CLOSE, kernel);
        Imgproc.morphologyEx(maskVerm, maskVerm, Imgproc.MORPH_OPEN,  kernel);

        List<MatOfPoint> contornos = new ArrayList<>();
        Imgproc.findContours(maskVerm.clone(), contornos, new Mat(),
            Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        List<MatOfPoint> validos = new ArrayList<>();
        for (MatOfPoint c : contornos)
            if (Imgproc.contourArea(c) >= AREA_MIN) validos.add(c);

        return validos;
    }

    public String classifySign(Mat sign) {
        Mat hsv = new Mat();
        Imgproc.cvtColor(sign, hsv, Imgproc.COLOR_BGR2HSV);

        // ---------------------------------------------------------------
        // Passo 0: Razão de preenchimento vermelho no crop
        // ---------------------------------------------------------------
        double totalPixels = sign.rows() * sign.cols();
        double pixelsVerm  = contarPixelsVermelho(hsv);
        double razaoVerm   = pixelsVerm / totalPixels;

        System.out.printf("[classifySign] recorte=%dx%d  razaoVerm=%.3f%n",
            sign.cols(), sign.rows(), razaoVerm);

        if (razaoVerm > 0.55) {
            System.out.println("[classifySign] → PARE (preenchimento vermelho alto)");
            return "Pare";
        }

        // ---------------------------------------------------------------
        // Passo 1: Diagonal vermelha por proporção de linhas
        // ---------------------------------------------------------------
        boolean temDiagonal = temDiagonalPorProporcao(sign, hsv);
        System.out.println("[classifySign] temDiagonal=" + temDiagonal);

        if (!temDiagonal) {
            System.out.println("[classifySign] → VELOCIDADE MAXIMA");
            return "Velocidade maxima";
        }

        // ---------------------------------------------------------------
        // Passo 2: Seta interna
        // ---------------------------------------------------------------
        boolean temSeta = temSetaInterna(sign, hsv);
        System.out.println("[classifySign] temSeta=" + temSeta);

        if (temSeta) {
            System.out.println("[classifySign] → SENTIDO PROIBIDO");
            return "Sentido proibido";
        }
        System.out.println("[classifySign] → PROIBIDO ESTACIONAR");
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
            resultado.add(classifySign(new Mat(image, new Rect(x, y, w, h))));
        }
        return resultado;
    }

    // ===================================================================
    // Helpers
    // ===================================================================

    private double contarPixelsVermelho(Mat hsv) {
        Mat m1 = new Mat(), m2 = new Mat(), combined = new Mat();
        Core.inRange(hsv, new Scalar(0,   50, 50), new Scalar(10,  255, 255), m1);
        Core.inRange(hsv, new Scalar(160, 50, 50), new Scalar(179, 255, 255), m2);
        Core.bitwise_or(m1, m2, combined);
        return Core.countNonZero(combined);
    }

    private boolean temDiagonalPorProporcao(Mat sign, Mat hsv) {
        Mat m1 = new Mat(), m2 = new Mat(), maskVerm = new Mat();
        Core.inRange(hsv, new Scalar(0,   50, 50), new Scalar(10,  255, 255), m1);
        Core.inRange(hsv, new Scalar(160, 50, 50), new Scalar(179, 255, 255), m2);
        Core.bitwise_or(m1, m2, maskVerm);

        int    menorDim = Math.min(sign.rows(), sign.cols());
        int    minVotos = Math.max(8, menorDim / 20);
        double minLen   = menorDim / 8.0;

        Mat lines = new Mat();
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
        System.out.printf("[temDiagonalPorProporcao] total=%d diagonal=%d prop=%.2f%n",
            total, diagonal, prop);

        return prop > 0.28 && diagonal >= 3;
    }

    private boolean temSetaInterna(Mat sign, Mat hsv) {
        Mat m1 = new Mat(), m2 = new Mat(), maskVerm = new Mat(), maskNaoVerm = new Mat();
        Core.inRange(hsv, new Scalar(0,   40, 40), new Scalar(10,  255, 255), m1);
        Core.inRange(hsv, new Scalar(160, 40, 40), new Scalar(179, 255, 255), m2);
        Core.bitwise_or(m1, m2, maskVerm);
        Core.bitwise_not(maskVerm, maskNaoVerm);

        Mat gray = new Mat(), bin = new Mat();
        Imgproc.cvtColor(sign, gray, Imgproc.COLOR_BGR2GRAY);
        Core.bitwise_and(gray, gray, gray, maskNaoVerm);
        Imgproc.threshold(gray, bin, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);

        int margem  = sign.cols() / 6;
        Mat binCrop = new Mat(bin, new Rect(margem, margem,
            bin.cols() - 2 * margem, bin.rows() - 2 * margem));

        int    menorDim = Math.min(binCrop.rows(), binCrop.cols());
        int    minVotos = Math.max(10, menorDim / 10);
        double minLen   = menorDim / 5.0;

        Mat lines = new Mat();
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

        System.out.printf("[temSetaInterna] linhas total=%d  horiz=%d  vert=%d%n",
            lines.rows(), horizontais, verticais);

        boolean ehSeta = horizontais < 5;
        System.out.println("[temSetaInterna] ehSeta=" + ehSeta);
        return ehSeta;
    }
}