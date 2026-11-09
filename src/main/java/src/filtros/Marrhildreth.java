package src.filtros;


import java.awt.image.BufferedImage;


public class Marrhildreth {

    // Desvio padrão da Gaussiana (σ) — controla o nível de suavização
    private final double sigma;

    // Limiar T para o zero crossing — filtra mudanças de sinal fracas (ruído)
    private final int limiar;

    public Marrhildreth(double sigma, int limiar) {
        if (!Double.isFinite(sigma) || sigma < 0.1 || sigma > 5 || limiar < 0 || limiar > 2040)
            throw new IllegalArgumentException("Sigma deve estar entre 0.1 e 5 e T entre 0 e 2040.");
        this.sigma  = sigma;
        this.limiar = limiar;
    }


    public BufferedImage aplicarFiltro(BufferedImage imagem) {
        int largura = imagem.getWidth();
        int altura  = imagem.getHeight();

        // --- Etapa 1: Suavização Gaussiana ---
        // Fs(x,y) ← convolução de f(x,y) com G(x,y,σ)
        double[][] kernel = gerarKernelGaussiano(sigma);
        int[][] fs = aplicarGaussiano(imagem, kernel);

        // --- Etapa 2: Operador Laplaciano ---
        // L(x,y) ← ∇² Fs(x,y)
        // Usa a máscara discreta do Laplaciano 3x3:
        //   [ 0  1  0 ]
        //   [ 1 -4  1 ]
        //   [ 0  1  0 ]
        int[][] laplaciano = aplicarLaplaciano(fs, largura, altura);

        // --- Etapa 3: Zero crossing com limiar T ---
        // B(x,y) = 255 se houver mudança de sinal na vizinhança com |diff| > T
        BufferedImage bordas = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
        for (int x = 1; x < largura - 1; x++) {
            for (int y = 1; y < altura - 1; y++) {
                int valor = detectarZeroCrossing(laplaciano, x, y) ? 255 : 0;
                int pixel = (valor << 16) | (valor << 8) | valor;
                bordas.setRGB(x, y, pixel);
            }
        }

        return bordas;
    }

    // ----------------------------------------------------------------
    // Etapa 1 — Filtro Gaussiano
    // ----------------------------------------------------------------

    /**
     * Gera o kernel Gaussiano discreto de tamanho (6σ+1) x (6σ+1).
     * G(x,y,σ) = (1 / 2πσ²) * e^(-(x²+y²) / 2σ²)
     */
    private double[][] gerarKernelGaussiano(double sigma) {
        int tamanho = (int) Math.ceil(6 * sigma) | 1; // garante tamanho ímpar
        int raio    = tamanho / 2;
        double[][] kernel = new double[tamanho][tamanho];
        double soma = 0;

        for (int i = -raio; i <= raio; i++) {
            for (int j = -raio; j <= raio; j++) {
                double valor = Math.exp(-(i * i + j * j) / (2 * sigma * sigma));
                kernel[i + raio][j + raio] = valor;
                soma += valor;
            }
        }

        // Normaliza para que a soma do kernel seja 1
        for (int i = 0; i < tamanho; i++) {
            for (int j = 0; j < tamanho; j++) {
                kernel[i][j] /= soma;
            }
        }

        return kernel;
    }

    /**
     * Converte a imagem para tons de cinza e aplica a convolução Gaussiana.
     * Retorna Fs(x,y) como matriz int[largura][altura].
     */
    private int[][] aplicarGaussiano(BufferedImage imagem, double[][] kernel) {
        int largura = imagem.getWidth();
        int altura  = imagem.getHeight();
        int raio    = kernel.length / 2;
        int[][] fs  = new int[largura][altura];

        for (int x = 0; x < largura; x++) {
            for (int y = 0; y < altura; y++) {
                double soma = 0;

                for (int i = -raio; i <= raio; i++) {
                    for (int j = -raio; j <= raio; j++) {
                        // Replicação do pixel mais próximo na borda
                        int xi = Math.max(0, Math.min(x + i, largura - 1));
                        int yi = Math.max(0, Math.min(y + j, altura - 1));

                        int rgb = imagem.getRGB(xi, yi);
                        int cinza = cinza(rgb);

                        soma += cinza * kernel[i + raio][j + raio];
                    }
                }

                fs[x][y] = (int) Math.round(soma);
            }
        }

        return fs;
    }

    // ----------------------------------------------------------------
    // Etapa 2 — Operador Laplaciano
    // ----------------------------------------------------------------

    /**
     * Aplica a máscara discreta do Laplaciano sobre Fs(x,y):
     *
     *   [ 0  1  0 ]
     *   [ 1 -4  1 ]
     *   [ 0  1  0 ]
     *
     * L(x,y) = Fs(x-1,y) + Fs(x+1,y) + Fs(x,y-1) + Fs(x,y+1) - 4*Fs(x,y)
     *
     * O resultado pode ser negativo ou positivo — mantemos o sinal
     * para a detecção de zero crossing na etapa seguinte.
     */
    private int[][] aplicarLaplaciano(int[][] fs, int largura, int altura) {
        int[][] L = new int[largura][altura];

        for (int x = 1; x < largura - 1; x++) {
            for (int y = 1; y < altura - 1; y++) {
                L[x][y] = fs[x - 1][y] + fs[x + 1][y]
                         + fs[x][y - 1] + fs[x][y + 1]
                         - 4 * fs[x][y];
            }
        }

        return L;
    }

    // ----------------------------------------------------------------
    // Etapa 3 — Zero Crossing com limiar T
    // ----------------------------------------------------------------

    /**
     * Detecta zero crossing no pixel (x,y):
     * Verifica os 4 pares de vizinhos opostos (horizontal, vertical e diagonais).
     * Há zero crossing se um vizinho é positivo, o oposto é negativo (ou vice-versa),
     * e a diferença absoluta entre eles supera o limiar T.
     *
     * @return true se (x,y) é borda
     */
    private boolean detectarZeroCrossing(int[][] L, int x, int y) {
        // Pares de vizinhos opostos: (esquerda/direita), (cima/baixo), diagonais
        int[][] pares = {
            { L[x - 1][y],     L[x + 1][y]     }, // horizontal
            { L[x][y - 1],     L[x][y + 1]     }, // vertical
            { L[x - 1][y - 1], L[x + 1][y + 1] }, // diagonal \
            { L[x - 1][y + 1], L[x + 1][y - 1] }  // diagonal /
        };

        for (int[] par : pares) {
            int a = par[0];
            int b = par[1];
            // Mudança de sinal + diferença absoluta acima do limiar
            if (((a > 0 && b < 0) || (a < 0 && b > 0))
                    && Math.abs(a - b) > limiar) {
                return true;
            }
        }

        return false;
    }

    // ----------------------------------------------------------------
    // Utilitário
    // ----------------------------------------------------------------

    /** Converte um pixel RGB para tom de cinza pela média dos canais. */
    private int cinza(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8)  & 0xFF;
        int b =  rgb        & 0xFF;
        return (r + g + b) / 3;
    }
}
