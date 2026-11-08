package src.filtros;

import java.awt.image.BufferedImage;

public class Contraste {

    public BufferedImage aplicarContraste(BufferedImage image, float fatorContraste) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage resultImage = new BufferedImage(width, height, image.getType());

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
                int newRgb = (red << 16) | (green << 8) | blue;
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
