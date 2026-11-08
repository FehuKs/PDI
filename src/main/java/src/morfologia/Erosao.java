package src.morfologia;

import java.awt.image.BufferedImage;

public class Erosao {

    public BufferedImage aplicarFiltro(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage resultImage = new BufferedImage(width, height, image.getType());

        for (int channel = 0; channel < 3; channel++) { // Itera pelos canais (R, G, B)
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                        // Copia os valores originais para os pixels de borda
                        int originalValue = extractChannel(image.getRGB(x, y), channel);
                        setPixel(resultImage, channel, x, y, originalValue);
                    } else {
                        // Aplica a erosão para os pixels internos
                        int newValue = computeCenter(image, channel, x, y);
                        setPixel(resultImage, channel, x, y, newValue);
                    }
                }
            }
        }
        return resultImage;
    }

    private int computeCenter(BufferedImage image, int channel, int x, int y) {
        int smallest = Integer.MAX_VALUE;

        for (int lx = -1; lx <= 1; lx++) {
            for (int ly = -1; ly <= 1; ly++) {
                int ix = limit(x + lx, 0, image.getWidth() - 1);
                int iy = limit(y + ly, 0, image.getHeight() - 1);

                int rgb = image.getRGB(ix, iy);
                int value = extractChannel(rgb, channel);

                if (value < smallest) {
                    smallest = value;
                }
            }
        }
        return smallest;
    }

    private int extractChannel(int rgb, int channel) {
        return (rgb >> (8 * (2 - channel))) & 0xFF; // Extrai o valor do canal
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

    private int limit(int value, int min, int max) {
        return Math.max(min, Math.min(max, value)); // Garante que está dentro do intervalo
    }
}