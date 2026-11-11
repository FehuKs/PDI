package src.desafio;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import src.infra.CvScope;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/** Font-derived templates; caches Java bytes, never native matrices or input images. */
final class GlyphTemplates {
    private record Template(char value, byte[] pixels) {}

    private final int size;
    private final List<Template> templates;

    static GlyphTemplates letters(int size, int margin) {
        return new GlyphTemplates(
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ", size, margin, new int[] {Font.BOLD}, true);
    }

    static GlyphTemplates digits(int size, int margin) {
        return new GlyphTemplates(
                "0123456789", size, margin, new int[] {Font.PLAIN, Font.BOLD}, false);
    }

    private GlyphTemplates(String alphabet, int size, int margin, int[] styles, boolean heavy) {
        this.size = size;
        List<Template> generated = new ArrayList<>();
        Mat stroke =
                CvScope.track(Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3)));
        for (char value : alphabet.toCharArray()) {
            for (String family : new String[] {"SansSerif", "Serif"}) {
                for (int style : styles) {
                    Mat glyph = render(value, new Font(family, style, 160), size, margin);
                    generated.add(new Template(value, bytes(glyph)));
                    if (heavy) {
                        Mat thicker = CvScope.track(new Mat());
                        Imgproc.dilate(glyph, thicker, stroke);
                        generated.add(new Template(value, bytes(thicker)));
                    }
                }
            }
        }
        templates = List.copyOf(generated);
    }

    char classify(Mat glyph, double minimumScore) {
        if (glyph.rows() != size || glyph.cols() != size || glyph.channels() != 1) {
            throw new IllegalArgumentException(
                    "Dimensões de glifo incompatíveis com os templates.");
        }
        byte[] input = bytes(glyph);
        char best = '?';
        double bestScore = -1;
        for (Template template : templates) {
            int intersection = 0, union = 0;
            for (int i = 0; i < input.length; i++) {
                boolean a = input[i] != 0, b = template.pixels()[i] != 0;
                if (a && b) intersection++;
                if (a || b) union++;
            }
            double score = union == 0 ? 0 : intersection / (double) union;
            if (score > bestScore) {
                bestScore = score;
                best = template.value();
            }
        }
        return bestScore >= minimumScore ? best : '?';
    }

    private static byte[] bytes(Mat glyph) {
        byte[] pixels = new byte[(int) glyph.total()];
        glyph.get(0, 0, pixels);
        return pixels;
    }

    private static Mat render(char value, Font font, int size, int margin) {
        BufferedImage image = new BufferedImage(240, 240, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = image.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.BLACK);
            g.setFont(font);
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(
                    String.valueOf(value),
                    (240 - metrics.charWidth(value)) / 2,
                    (240 - metrics.getHeight()) / 2 + metrics.getAscent());
        } finally {
            g.dispose();
        }
        Mat raw = OpenCVUtils.toMatGray(image), binary = CvScope.track(new Mat());
        Imgproc.threshold(raw, binary, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
        return OpenCVUtils.normalizarGlifo(binary, size, margin);
    }
}
