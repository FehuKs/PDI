package src.filtros;

import src.core.Images;

import java.awt.image.BufferedImage;

public class Robinson {

    // Definição das 8 máscaras de Robinson
    private final int[][][] masks = {
        {
            {1, 2, 1},
            {0, 0, 0},
            {-1, -2, -1}
        },
        {
            {0, 1, 2},
            {-1, 0, 1},
            {-2, -1, 0}
        },
        {
            {-1, 0, 1},
            {-2, 0, 2},
            {-1, 0, 1}
        },
        {
            {-2, -1, 0},
            {-1, 0, 1},
            {0, 1, 2}
        },
        {
            {-1, -2, -1},
            {0, 0, 0},
            {1, 2, 1}
        },
        {
            {0, -1, -2},
            {1, 0, -1},
            {2, 1, 0}
        },
        {
            {1, 0, -1},
            {2, 0, -2},
            {1, 0, -1}
        },
        {
            {2, 1, 0},
            {1, 0, -1},
            {0, -1, -2}
        }
    };

    public BufferedImage aplicarFiltro(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage resultImage = Images.like(image);

        // Aplica a convolução em cada canal (R, G, B)
        for (int channel = 0; channel < 3; channel++) {
            for (int x = 1; x < width - 1; x++) {
                for (int y = 1; y < height - 1; y++) {
                    int newValue = calcularPixel(image, channel, x, y);
                    resultImage.setRGB(x, y, setValorNoCanal(resultImage.getRGB(x, y), channel, newValue));
                }
            }
        }
        return resultImage;
    }

    private int calcularPixel(BufferedImage image, int channel, int x, int y) {
        int[] gradients = new int[masks.length];

        // Calcula o valor para cada máscara
        for (int i = 0; i < masks.length; i++) {
            gradients[i] = calcularGradiente(image, channel, x, y, masks[i]);
        }

        // Processa os gradientes para determinar o valor final do pixel
        return processarGradientes(gradients);
    }

    private int calcularGradiente(BufferedImage image, int channel, int x, int y, int[][] mask) {
        int sum = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int xPos = x + (i - 1);
                int yPos = y + (j - 1);
                int rgb = image.getRGB(xPos, yPos);
                int valor = (rgb >> (8 * (2 - channel))) & 0xFF;
                sum += valor * mask[i][j];
            }
        }
        return sum;
    }

    private int processarGradientes(int[] gradients) {
        int maxGradient = 0;
        for (int gradient : gradients) {
            maxGradient = Math.max(maxGradient, Math.abs(gradient));
        }
        return Math.min(255, maxGradient * 256 / 1024);
    }

    private int setValorNoCanal(int rgb, int channel, int value) {
        int[] rgbChannels = {
            (rgb >> 16) & 0xFF,  // Red
            (rgb >> 8) & 0xFF,   // Green
            rgb & 0xFF           // Blue
        };
        rgbChannels[channel] = value;
        return (rgb & 0xff000000) | (rgbChannels[0] << 16) | (rgbChannels[1] << 8) | rgbChannels[2];
    }
}
