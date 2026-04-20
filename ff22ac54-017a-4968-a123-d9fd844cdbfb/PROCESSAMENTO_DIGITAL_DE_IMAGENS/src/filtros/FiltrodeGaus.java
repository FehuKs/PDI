package src.filtros;


import java.awt.image.BufferedImage;

public class FiltrodeGaus {

    // Kernel Gaussiano 3x3
    private final double[][] kernel = {
            {1, 2, 1},
            {2, 4, 2},
            {1, 2, 1}
    };

    public BufferedImage aplicarFiltro(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage resultImage = new BufferedImage(width, height, image.getType());

        // Aplica a convolução Gaussiana
        for (int channel = 0; channel < 3; channel++) {  // Itera pelos canais (R, G, B)
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    aplicarKernel(image, resultImage, channel, x, y);
                }
            }
        }
        return resultImage;
    }

    private void aplicarKernel(BufferedImage image, BufferedImage resultImage, int channel, int x, int y) {
        double sumValue = 0;
        double valueKernel = 0;
        int ksize = kernel.length;

        for (int i = 0; i < ksize; i++) {
            for (int j = 0; j < ksize; j++) {
                int xPos = limitarX(x + (i - 1), image.getWidth());
                int yPos = limitarY(y + (j - 1), image.getHeight());
                int rgb = image.getRGB(xPos, yPos);

                // Extrai o valor do canal (R, G ou B)
                int value = (rgb >> (8 * (2 - channel))) & 0xFF;
                sumValue += value * kernel[i][j];
                valueKernel += kernel[i][j];
            }
        }

        if (valueKernel > 0) {
            sumValue /= valueKernel;
        }

        // Garante que o valor está no intervalo [0, 255]
        int newValue = Math.min(255, Math.max(0, (int) Math.round(sumValue)));

        // Define o novo valor no canal correto da imagem resultante
        int rgbOriginal = resultImage.getRGB(x, y);
        int[] rgbChannels = {
            (rgbOriginal >> 16) & 0xFF, // Red
            (rgbOriginal >> 8) & 0xFF,  // Green
            rgbOriginal & 0xFF          // Blue
        };
        rgbChannels[channel] = newValue;

        // Combina os valores dos canais em um novo pixel
        int newRgb = (rgbChannels[0] << 16) | (rgbChannels[1] << 8) | rgbChannels[2];
        resultImage.setRGB(x, y, newRgb);
    }

    private int limitarX(int x, int width) {
        return Math.max(0, Math.min(x, width - 1));
    }

    private int limitarY(int y, int height) {
        return Math.max(0, Math.min(y, height - 1));
    }
}
