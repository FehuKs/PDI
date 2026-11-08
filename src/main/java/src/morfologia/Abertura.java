package src.morfologia;


import java.awt.image.BufferedImage;

public class Abertura {

    public BufferedImage aplicarFiltro(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Aplica a erosão
        BufferedImage erosionResult = new BufferedImage(width, height, image.getType());
        aplicarErosao(image, erosionResult);

        // Aplica a dilatação
        BufferedImage dilationResult = new BufferedImage(width, height, image.getType());
        aplicarDilatacao(erosionResult, dilationResult);

        return dilationResult;
    }

    private void aplicarErosao(BufferedImage image, BufferedImage resultImage) {
        aplicarConvolucao(image, resultImage, true);
    }

    private void aplicarDilatacao(BufferedImage image, BufferedImage resultImage) {
        aplicarConvolucao(image, resultImage, false);
    }

    private void aplicarConvolucao(BufferedImage image, BufferedImage resultImage, boolean isErosion) {
        int width = image.getWidth();
        int height = image.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    // Preserva os pixels de borda
                    resultImage.setRGB(x, y, image.getRGB(x, y));
                } else {
                    int[] maxMinRGB = processarVizinhos(image, x, y, isErosion);
                    int newRGB = (maxMinRGB[0] << 16) | (maxMinRGB[1] << 8) | maxMinRGB[2];
                    resultImage.setRGB(x, y, newRGB);
                }
            }
        }
    }

    private int[] processarVizinhos(BufferedImage image, int x, int y, boolean isErosion) {
        int[] resultado = new int[3]; // Para os canais R, G e B
        int[][] vizinhos = new int[3][9]; // Matriz para armazenar valores dos 3 canais

        int index = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int rgb = image.getRGB(x + i, y + j);
                vizinhos[0][index] = (rgb >> 16) & 0xFF; // Canal Red
                vizinhos[1][index] = (rgb >> 8) & 0xFF;  // Canal Green
                vizinhos[2][index] = rgb & 0xFF;         // Canal Blue
                index++;
            }
        }

        for (int channel = 0; channel < 3; channel++) {
            if (isErosion) {
                resultado[channel] = encontrarMenor(vizinhos[channel]);
            } else {
                resultado[channel] = encontrarMaior(vizinhos[channel]);
            }
        }

        return resultado;
    }

    private int encontrarMenor(int[] values) {
        int menor = Integer.MAX_VALUE;
        for (int v : values) {
            if (v < menor) {
                menor = v;
            }
        }
        return menor;
    }

    private int encontrarMaior(int[] values) {
        int maior = Integer.MIN_VALUE;
        for (int v : values) {
            if (v > maior) {
                maior = v;
            }
        }
        return maior;
    }
}