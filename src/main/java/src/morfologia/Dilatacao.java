package src.morfologia;

import src.core.Images;

import java.awt.image.BufferedImage;

public class Dilatacao {

    public BufferedImage aplicarFiltro(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage resultImage = Images.like(image);

        // Aplica a dilatação
        for (int channel = 0; channel < 3; channel++) { // Itera pelos canais (R, G, B)
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                        // Copia os valores originais para os pixels de borda
                        int originalValue = extractChannel(image.getRGB(x, y), channel);
                        setPixel(resultImage, channel, x, y, originalValue);
                    } else {
                        // Aplica a dilatação para os pixels internos
                        int newValue = computeCenter(image, channel, x, y);
                        setPixel(resultImage, channel, x, y, newValue);
                    }
                }
            }
        }
        return resultImage;
    }

    private int computeCenter(BufferedImage image, int channel, int x, int y) {
        int largest = 0;

        // Itera pela vizinhança de 3x3
        for (int lx = -1; lx <= 1; lx++) {
            for (int ly = -1; ly <= 1; ly++) {
                int ix = limit(x + lx, image.getWidth());
                int iy = limit(y + ly, image.getHeight());

                int rgb = image.getRGB(ix, iy);
                int value = extractChannel(rgb, channel);

                if (value > largest) {
                    largest = value;
                }
            }
        }
        return largest;
    }

    private int limit(int coordinate, int max) {
        return Math.max(0, Math.min(coordinate, max - 1));
    }

    private int extractChannel(int rgb, int channel) {
        return (rgb >> (8 * (2 - channel))) & 0xFF;
    }

    private void setPixel(BufferedImage image, int channel, int x, int y, int value) {
        int rgbOriginal = image.getRGB(x, y);
        int[] rgbChannels = {
                (rgbOriginal >> 16) & 0xFF, // Red
                (rgbOriginal >> 8) & 0xFF,  // Green
                rgbOriginal & 0xFF          // Blue
        };
        rgbChannels[channel] = value;

        int newRgb = (rgbOriginal & 0xff000000) | (rgbChannels[0] << 16) | (rgbChannels[1] << 8) | rgbChannels[2];
        image.setRGB(x, y, newRgb);
    }
}
