package src.morfologia;

import java.awt.image.BufferedImage;

public class Fechamento {

    public BufferedImage aplicarFiltro(BufferedImage image) {
        BufferedImage dilationResultImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        BufferedImage erosionResultImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        // Etapa de dilatação
        dilatacao(image, dilationResultImage);

        // Etapa de erosão
        erosao(dilationResultImage, erosionResultImage);

        return erosionResultImage;
    }

    private void dilatacao(BufferedImage image, BufferedImage resultImage) {
        aplicarConvolucao(image, resultImage, this::maximoVizinho);
    }

    private void erosao(BufferedImage image, BufferedImage resultImage) {
        aplicarConvolucao(image, resultImage, this::minimoVizinho);
    }

    private void aplicarConvolucao(BufferedImage image, BufferedImage resultImage, VizinhoOperation operation) {
        int width = image.getWidth();
        int height = image.getHeight();

        for (int channel = 0; channel < 3; channel++) { // Itera pelos canais (R, G, B)
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                        // Copia os valores originais nas bordas
                        int originalValue = (image.getRGB(x, y) >> (8 * (2 - channel))) & 0xFF;
                        setPixel(resultImage, channel, x, y, originalValue);
                    } else {
                        // Aplica a operação nos pixels internos
                        int[][] vizinhos = obterVizinhos(image, channel, x, y);
                        int novoValor = operation.calcular(vizinhos);
                        setPixel(resultImage, channel, x, y, novoValor);
                    }
                }
            }
        }
    }

    private int[][] obterVizinhos(BufferedImage image, int channel, int x, int y) {
        int[][] vizinhos = new int[3][3];
        for (int lx = 0; lx < 3; lx++) {
            for (int ly = 0; ly < 3; ly++) {
                int ix = limitar(x + lx - 1, 0, image.getWidth() - 1);
                int iy = limitar(y + ly - 1, 0, image.getHeight() - 1);

                int rgb = image.getRGB(ix, iy);
                vizinhos[lx][ly] = (rgb >> (8 * (2 - channel))) & 0xFF; // Extrai o canal
            }
        }
        return vizinhos;
    }

    private int maximoVizinho(int[][] vizinhos) {
        int maximo = 0;
        for (int[] linha : vizinhos) {
            for (int valor : linha) {
                maximo = Math.max(maximo, valor);
            }
        }
        return maximo;
    }

    private int minimoVizinho(int[][] vizinhos) {
        int minimo = Integer.MAX_VALUE;
        for (int[] linha : vizinhos) {
            for (int valor : linha) {
                minimo = Math.min(minimo, valor);
            }
        }
        return minimo;
    }

    private void setPixel(BufferedImage image, int channel, int x, int y, int value) {
        int rgbOriginal = image.getRGB(x, y);
        int[] rgbChannels = {
                (rgbOriginal >> 16) & 0xFF,
                (rgbOriginal >> 8) & 0xFF,
                rgbOriginal & 0xFF
        };
        rgbChannels[channel] = value;

        int newRgb = (rgbChannels[0] << 16) | (rgbChannels[1] << 8) | rgbChannels[2];
        image.setRGB(x, y, newRgb);
    }

    private int limitar(int valor, int min, int max) {
        return Math.max(min, Math.min(max, valor));
    }

    @FunctionalInterface
    interface VizinhoOperation {
        int calcular(int[][] vizinhos);
    }
}