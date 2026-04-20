package src.filtros;

import java.awt.image.BufferedImage;

public class Brilho {

    private static final int MAX_RGB = 255;
    private static final int MIN_RGB = 0;

    /**
     * Aplica o ajuste de brilho em cada canal da imagem.
     * 
     * @param originalImage Imagem original a ser ajustada
     * @param brilho Valor de ajuste de brilho (positivo para aumentar, negativo para diminuir)
     * @return Imagem ajustada com o novo brilho
     */
    public BufferedImage aplicarBrilho(BufferedImage originalImage, int brilho) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        if (width == 0 || height == 0) {
            throw new IllegalArgumentException("Imagem vazia ou inválida.");
        }

        // Cria uma nova imagem vazia com as mesmas dimensões e tipo
        BufferedImage resultImage = new BufferedImage(width, height, originalImage.getType());

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
                int newRGB = (newR << 16) | (newG << 8) | newB;

                // Define o novo valor ajustado na imagem de resultado
                resultImage.setRGB(x, y, newRGB);
            }
        }

        return resultImage;
    }

    /**
     * Limita o valor do pixel entre MIN_RGB e MAX_RGB.
     */
    private int limitPixelValue(int value) {
        if (value > MAX_RGB) return MAX_RGB;
        if (value < MIN_RGB) return MIN_RGB;
        return value;
    }
}
