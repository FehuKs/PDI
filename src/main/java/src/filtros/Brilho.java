package src.filtros;

import src.core.Images;

import java.awt.image.BufferedImage;

public class Brilho {

    private static final int MAX_RGB = 255;
    private static final int MIN_RGB = 0;


    public BufferedImage aplicarBrilho(BufferedImage originalImage, int brilho) {
        Images.require(originalImage);
        if (brilho < -255 || brilho > 255) throw new IllegalArgumentException("Brilho deve estar entre -255 e 255.");
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        if (width == 0 || height == 0) {
            throw new IllegalArgumentException("Imagem vazia ou inválida.");
        }

        // Cria uma nova imagem vazia com as mesmas dimensões e tipo
        BufferedImage resultImage = Images.like(originalImage);

        // Itera sobre cada canal RGB (R=0, G=1, B=2) e cada pixel
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = originalImage.getRGB(x, y);

                // Extrai os valores individuais de Red, Green, Blue
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Aplica o brilho a cada canal e limita o valor no intervalo correto
                int newR = limitPixelValue(r + brilho);
                int newG = limitPixelValue(g + brilho);
                int newB = limitPixelValue(b + brilho);

                // Reconstroi o valor RGB com os canais ajustados
                int newRGB = (rgb & 0xff000000) | (newR << 16) | (newG << 8) | newB;

                // Define o novo valor ajustado na imagem de resultado
                resultImage.setRGB(x, y, newRGB);
            }
        }

        return resultImage;
    }

    private int limitPixelValue(int value) {
        if (value > MAX_RGB) return MAX_RGB;
        if (value < MIN_RGB) return MIN_RGB;
        return value;
    }
}
