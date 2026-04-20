package src.filtros;

import java.awt.image.BufferedImage;

public class Limiarizacao {

    public BufferedImage aplicarLimiarizacao(BufferedImage image, int threshold) {
        // Primeiro, convertendo a imagem para escala de cinza
        BufferedImage grayscaleImage = aplicarGrayscale(image);
        BufferedImage resultImage = new BufferedImage(grayscaleImage.getWidth(), grayscaleImage.getHeight(), BufferedImage.TYPE_BYTE_BINARY);

        // Aplicando o limiar
        for (int x = 0; x < grayscaleImage.getWidth(); x++) {
            for (int y = 0; y < grayscaleImage.getHeight(); y++) {
                int pixel = grayscaleImage.getRGB(x, y);
                // Extraindo o valor de cinza do pixel
                int grayValue = (pixel >> 16) & 0xFF;

                // Comparando com o threshold
                int newPixel = grayValue >= threshold ? 0xFFFFFF : 0x000000;

                resultImage.setRGB(x, y, newPixel);
            }
        }

        return resultImage;
    }

    // Método auxiliar para converter a imagem para escala de cinza
    private BufferedImage aplicarGrayscale(BufferedImage image) {
        BufferedImage grayscaleImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                // Calculando o valor de cinza (média dos canais R, G e B)
                int grayValue = (red + green + blue) / 3;
                int grayPixel = (grayValue << 16) | (grayValue << 8) | grayValue;

                grayscaleImage.setRGB(x, y, grayPixel);
            }
        }

        return grayscaleImage;
    }
}
