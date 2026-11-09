package src.filtros;

import src.core.Images;
import java.awt.image.BufferedImage;

public class Grayscale {

    public BufferedImage aplicarGrayscale(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage resultImage = Images.like(image);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = image.getRGB(x, y);

                // Extrair os canais de cor (R, G, B)
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                // Calcular a média dos canais
                int grayscale = (red + green + blue) / 3;

                // Criar a nova cor em escala de cinza
                int newPixel = (rgb & 0xff000000) | (grayscale << 16) | (grayscale << 8) | grayscale;

                // Definir o novo valor do pixel na imagem resultante
                resultImage.setRGB(x, y, newPixel);
            }
        }

        return resultImage;
    }
}
