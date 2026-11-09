package src.filtros;

import java.awt.image.BufferedImage;

public class Limiarizacao {
    public BufferedImage aplicarLimiarizacao(BufferedImage image, int threshold) {
        if (threshold < 0 || threshold > 255) throw new IllegalArgumentException("Limiar deve estar entre 0 e 255.");
        BufferedImage gray = new Grayscale().aplicarGrayscale(image);
        for (int y = 0; y < gray.getHeight(); y++) {
            for (int x = 0; x < gray.getWidth(); x++) {
                int pixel = gray.getRGB(x, y);
                int color = (pixel & 255) >= threshold ? 0xffffff : 0;
                gray.setRGB(x, y, (pixel & 0xff000000) | color);
            }
        }
        return gray;
    }
}
