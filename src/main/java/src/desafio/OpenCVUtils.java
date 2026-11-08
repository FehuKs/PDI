package src.desafio;

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
        BufferedImage bgr = new BufferedImage(
            img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        bgr.getGraphics().drawImage(img, 0, 0, null);
        byte[] data = ((DataBufferByte) bgr.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(bgr.getHeight(), bgr.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);
        return mat;
    }

    /** Converte BufferedImage para Mat CV_8UC1 (tons de cinza/binária). */
    public static Mat toMatGray(BufferedImage img) {
        BufferedImage g = new BufferedImage(
            img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        g.getGraphics().drawImage(img, 0, 0, null);
        byte[] data = ((DataBufferByte) g.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(g.getHeight(), g.getWidth(), CvType.CV_8UC1);
        mat.put(0, 0, data);
        return mat;
    }

    /** Redimensiona e centraliza um glifo para padronização. */
    
    public static Mat normalizarGlifo(Mat bin, int norm, int margem) {
        Mat naoZero = new Mat();
        Core.findNonZero(bin, naoZero);
        if (naoZero.empty()) return Mat.zeros(norm, norm, CvType.CV_8UC1);
        
        Rect bb = Imgproc.boundingRect(naoZero);
        Mat crop = new Mat(bin, bb);
        int alvo = norm - 2 * margem;
        double escala = Math.min((double) alvo / crop.cols(), (double) alvo / crop.rows());
        int novaW = Math.max(1, (int) Math.round(crop.cols() * escala));
        int novaH = Math.max(1, (int) Math.round(crop.rows() * escala));
        
        Mat resized = new Mat();
        Imgproc.resize(crop, resized, new Size(novaW, novaH));
        Mat canvas = Mat.zeros(norm, norm, CvType.CV_8UC1);
        resized.copyTo(canvas.submat(new Rect((norm - novaW) / 2, (norm - novaH) / 2, novaW, novaH)));
        return canvas;
    }

    /** Compara dois glifos usando XOR. */
    public static double comparar(Mat a, Mat b, int norm) {
        Mat xor = new Mat();
        Core.bitwise_xor(a, b, xor);
        return 1.0 - (double) Core.countNonZero(xor) / (norm * norm);
    }
}