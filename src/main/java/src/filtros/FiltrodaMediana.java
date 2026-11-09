package src.filtros;

import src.core.Images;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class FiltrodaMediana {

    public BufferedImage aplicarFiltroMediana(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage resultImage = Images.like(image);

        for (int c = 0; c < 3; c++) { // Assumindo que estamos lidando com RGB (3 canais)
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int[][] neighbours = new int[3][3];

                    for (int lx = 0; lx < 3; lx++) {
                        for (int ly = 0; ly < 3; ly++) {
                            int ix = Math.min(Math.max(x + lx - 1, 0), width - 1);
                            int iy = Math.min(Math.max(y + ly - 1, 0), height - 1);
                            neighbours[lx][ly] = getPixelValue(image, ix, iy, c);
                        }
                    }

                    int newValue = computeMedian(neighbours);
                    setPixelValue(resultImage, x, y, c, newValue);
                }
            }
        }

        return resultImage;
    }

    private int computeMedian(int[][] neighbours) {
        int[] values = new int[9];
        int index = 0;
        for (int[] row : neighbours) {
            for (int value : row) {
                values[index++] = value;
            }
        }

        Arrays.sort(values);
        return values[values.length / 2]; // Retorna o valor mediano
    }

    private int getPixelValue(BufferedImage image, int x, int y, int channel) {
        int rgb = image.getRGB(x, y);
        switch (channel) {
            case 0: return (rgb >> 16) & 0xFF; // Canal vermelho
            case 1: return (rgb >> 8) & 0xFF;  // Canal verde
            case 2: return rgb & 0xFF;         // Canal azul
            default: return 0;
        }
    }

    private void setPixelValue(BufferedImage image, int x, int y, int channel, int value) {
        int rgb = image.getRGB(x, y);
        switch (channel) {
            case 0: rgb = (rgb & 0xFF00FFFF) | (value << 16); break; // Ajusta o vermelho
            case 1: rgb = (rgb & 0xFFFF00FF) | (value << 8);  break; // Ajusta o verde
            case 2: rgb = (rgb & 0xFFFFFF00) | value;         break; // Ajusta o azul
        }
        image.setRGB(x, y, rgb);
    }
}
