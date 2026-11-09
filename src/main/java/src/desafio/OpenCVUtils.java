package src.desafio;

import src.core.Images;
import src.infra.CvScope;
import src.infra.OpenCvRuntime;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class OpenCVUtils {

    /** Converte BufferedImage colorida para Mat BGR. */
    public static Mat toMat(BufferedImage img) {
        OpenCvRuntime.ensureLoaded();
        Images.require(img);
        BufferedImage bgr = new BufferedImage(
            img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        java.awt.Graphics2D graphics = bgr.createGraphics();
        graphics.setColor(java.awt.Color.WHITE); graphics.fillRect(0, 0, bgr.getWidth(), bgr.getHeight());
        graphics.drawImage(img, 0, 0, null); graphics.dispose();
        byte[] data = ((DataBufferByte) bgr.getRaster().getDataBuffer()).getData();
        Mat mat = CvScope.track(new Mat(bgr.getHeight(), bgr.getWidth(), CvType.CV_8UC3));
        mat.put(0, 0, data);
        return mat;
    }

    /** Converte BufferedImage para Mat CV_8UC1 (tons de cinza/binária). */
    public static Mat toMatGray(BufferedImage img) {
        OpenCvRuntime.ensureLoaded();
        Images.require(img);
        BufferedImage g = new BufferedImage(
            img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        java.awt.Graphics2D graphics = g.createGraphics();
        graphics.setColor(java.awt.Color.WHITE); graphics.fillRect(0, 0, g.getWidth(), g.getHeight());
        graphics.drawImage(img, 0, 0, null); graphics.dispose();
        byte[] data = ((DataBufferByte) g.getRaster().getDataBuffer()).getData();
        Mat mat = CvScope.track(new Mat(g.getHeight(), g.getWidth(), CvType.CV_8UC1));
        mat.put(0, 0, data);
        return mat;
    }

    /** Redimensiona e centraliza um glifo para padronização. */

    public static Mat normalizarGlifo(Mat bin, int norm, int margem) {
        Mat naoZero = CvScope.track(new Mat());
        Core.findNonZero(bin, naoZero);
        if (naoZero.empty()) return CvScope.track(Mat.zeros(norm, norm, CvType.CV_8UC1));

        Rect bb = Imgproc.boundingRect(naoZero);
        Mat crop = CvScope.track(new Mat(bin, bb));
        int alvo = norm - 2 * margem;
        double escala = Math.min((double) alvo / crop.cols(), (double) alvo / crop.rows());
        int novaW = Math.max(1, (int) Math.round(crop.cols() * escala));
        int novaH = Math.max(1, (int) Math.round(crop.rows() * escala));

        Mat resized = CvScope.track(new Mat());
        Imgproc.resize(crop, resized, new Size(novaW, novaH));
        // Restore a binary mask after interpolation so bitwise IoU measures foreground sets.
        Imgproc.threshold(resized, resized, 127, 255, Imgproc.THRESH_BINARY);
        Mat canvas = CvScope.track(Mat.zeros(norm, norm, CvType.CV_8UC1));
        resized.copyTo(CvScope.track(canvas.submat(new Rect((norm - novaW) / 2, (norm - novaH) / 2, novaW, novaH))));
        return canvas;
    }

    /** Foreground IoU: blank background cannot inflate the score. */
    public static double comparar(Mat a, Mat b, int norm) {
        if (norm < 1 || a.rows() != norm || a.cols() != norm || b.rows() != norm || b.cols() != norm)
            throw new IllegalArgumentException("Glifos devem ter as dimensões normalizadas.");
        Mat intersection = new Mat(), union = new Mat();
        try {
            Core.bitwise_and(a, b, intersection);
            Core.bitwise_or(a, b, union);
            int foreground = Core.countNonZero(union);
            return foreground == 0 ? 0 : (double) Core.countNonZero(intersection) / foreground;
        } finally { intersection.release(); union.release(); }
    }
}
