package src.afinamento;

import java.awt.image.BufferedImage;

public class ZhangSuen {

    private static final int LIMIAR = 128;

    public BufferedImage aplicarThinning(BufferedImage image) {
        int width  = image.getWidth();
        int height = image.getHeight();

        int[][] grid = toBinario(image, width, height);

        boolean houveRemocao;
        do {
            houveRemocao  = subPasso(grid, width, height, 1);
            houveRemocao |= subPasso(grid, width, height, 2);
        } while (houveRemocao);

        return toBufferedImage(grid, width, height);
    }

    private boolean subPasso(int[][] g, int w, int h, int passo) {
        boolean[][] marcados = new boolean[h][w];

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                if (g[y][x] != 1) continue;

                int p2 = g[y-1][x];
                int p3 = g[y-1][x+1];
                int p4 = g[y][x+1];
                int p5 = g[y+1][x+1];
                int p6 = g[y+1][x];
                int p7 = g[y+1][x-1];
                int p8 = g[y][x-1];
                int p9 = g[y-1][x-1];

                int B = p2 + p3 + p4 + p5 + p6 + p7 + p8 + p9;
                if (B < 2 || B > 6) continue;

                int A = contarTransicoes(p2, p3, p4, p5, p6, p7, p8, p9);
                if (A != 1) continue;

                if (passo == 1) {
                    if (p2 * p4 * p6 != 0) continue;
                    if (p4 * p6 * p8 != 0) continue;
                } else {
                    if (p2 * p4 * p8 != 0) continue;
                    if (p2 * p6 * p8 != 0) continue;
                }

                marcados[y][x] = true;
            }
        }

        boolean houveRemocao = false;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (marcados[y][x]) {
                    g[y][x] = 0;
                    houveRemocao = true;
                }
            }
        }
        return houveRemocao;
    }

    private int contarTransicoes(int p2, int p3, int p4, int p5,
                                  int p6, int p7, int p8, int p9) {
        int[] seq = {p2, p3, p4, p5, p6, p7, p8, p9, p2};
        int count = 0;
        for (int i = 0; i < 8; i++) {
            if (seq[i] == 0 && seq[i + 1] == 1) count++;
        }
        return count;
    }

    private int[][] toBinario(BufferedImage image, int w, int h) {
        int[][] grid = new int[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >>  8) & 0xFF;
                int b =  rgb        & 0xFF;
                double lum = 0.299 * r + 0.587 * g + 0.114 * b;
                grid[y][x] = (lum < LIMIAR) ? 1 : 0;
            }
        }
        return grid;
    }

    private BufferedImage toBufferedImage(int[][] grid, int w, int h) {
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                result.setRGB(x, y, grid[y][x] == 1 ? 0x000000 : 0xFFFFFF);
            }
        }
        return result;
    }
}