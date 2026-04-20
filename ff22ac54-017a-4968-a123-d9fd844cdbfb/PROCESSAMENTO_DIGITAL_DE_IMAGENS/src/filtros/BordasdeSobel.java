package src.filtros;

import java.awt.image.BufferedImage;

public class BordasdeSobel {

    // Máscaras de Sobel
    private final int[][] xMask = {
            {1, 0, -1},
            {2, 0, -2},
            {1, 0, -1}
    };

    private final int[][] yMask = {
            {1, 2, 1},
            {0, 0, 0},
            {-1, -2, -1}
    };

    public BufferedImage aplicarFiltro(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage resultImage = new BufferedImage(width, height, image.getType());

        // Aplica a convolução de Sobel
        for (int channel = 0; channel < 3; channel++) {  // Itera pelos canais (R, G, B)
            for (int x = 1; x < width - 1; x++) {
                for (int y = 1; y < height - 1; y++) {
                    int newValue = computePixel(image, channel, x, y);
                    setPixel(resultImage, channel, x, y, newValue);
                }
            }
        }
        return resultImage;
    }

    private int computePixel(BufferedImage image, int channel, int x, int y) {
        int gradientX = 0;
        int gradientY = 0;

        // Aplica as máscaras de Sobel para calcular os gradientes
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int xPos = x + i - 1;
                int yPos = y + j - 1;

                int rgb = image.getRGB(xPos, yPos);
                int value = (rgb >> (8 * (2 - channel))) & 0xFF;

                gradientX += value * xMask[i][j];
                gradientY += value * yMask[i][j];
            }
        }

        // Calcula a magnitude do gradiente
        int value = (int) Math.sqrt(gradientX * gradientX + gradientY * gradientY);
        return Math.min(255, Math.max(0, value));  // Garante que está no intervalo [0, 255]
    }

    private void setPixel(BufferedImage image, int channel, int x, int y, int value) {
        int rgbOriginal = image.getRGB(x, y);
        int[] rgbChannels = {
                (rgbOriginal >> 16) & 0xFF, // Red
                (rgbOriginal >> 8) & 0xFF,  // Green
                rgbOriginal & 0xFF          // Blue
        };
        rgbChannels[channel] = value;

        int newRgb = (rgbChannels[0] << 16) | (rgbChannels[1] << 8) | rgbChannels[2];
        image.setRGB(x, y, newRgb);
    }
}
