package src.filtros;

import src.core.Images;

import java.awt.image.BufferedImage;

public class Contraste {

    public BufferedImage aplicarContraste(BufferedImage image, float fatorContraste) {
        Images.require(image);
        if (!Float.isFinite(fatorContraste) || fatorContraste < 0 || fatorContraste > 10)
            throw new IllegalArgumentException("Contraste deve estar entre 0 e 10.");
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage resultImage = Images.like(image);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = image.getRGB(x, y);

                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                // Aplica o fator de contraste
                red = ajustarContraste(red, fatorContraste);
                green = ajustarContraste(green, fatorContraste);
                blue = ajustarContraste(blue, fatorContraste);

                // Recombina os valores ajustados no pixel
                int newRgb = (rgb & 0xff000000) | (red << 16) | (green << 8) | blue;
                resultImage.setRGB(x, y, newRgb);
            }
        }
        return resultImage;
    }

    private int ajustarContraste(int valor, float fatorContraste) {
        // Multiplica o valor pelo fator de contraste e ajusta ao intervalo [0, 255]
        int novoValor = (int)((valor - 128) * fatorContraste + 128);
        return Math.min(255, Math.max(0, novoValor));
    }
}
