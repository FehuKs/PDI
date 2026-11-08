package src.transformacoes;

import java.awt.image.BufferedImage;

public class TransformacaoMatricial {

   
    public static BufferedImage aplicar(BufferedImage origem, double[][] matriz, int largura, int altura) {
        BufferedImage destino = new BufferedImage(largura, altura, origem.getType());

        int origemW = origem.getWidth();
        int origemH = origem.getHeight();

        double[][] inv = inverter(matriz);

        for (int yd = 0; yd < altura; yd++) {
            for (int xd = 0; xd < largura; xd++) {

         
                double xo = inv[0][0] * xd + inv[0][1] * yd + inv[0][2];
                double yo = inv[1][0] * xd + inv[1][1] * yd + inv[1][2];

                int xi = (int) Math.round(xo);
                int yi = (int) Math.round(yo);

                if (xi >= 0 && xi < origemW && yi >= 0 && yi < origemH) {
                    destino.setRGB(xd, yd, origem.getRGB(xi, yi));
                }
            }
        }

        return destino;
    }

    private static double[][] inverter(double[][] m) {
        double det = m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1])
                   - m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0])
                   + m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0]);

        if (Math.abs(det) < 1e-10) {
            throw new ArithmeticException("Matriz singular: não é possível calcular a inversa.");
        }

        double invDet = 1.0 / det;

        return new double[][] {
            {
                invDet * (m[1][1] * m[2][2] - m[1][2] * m[2][1]),
                invDet * (m[0][2] * m[2][1] - m[0][1] * m[2][2]),
                invDet * (m[0][1] * m[1][2] - m[0][2] * m[1][1])
            },
            {
                invDet * (m[1][2] * m[2][0] - m[1][0] * m[2][2]),
                invDet * (m[0][0] * m[2][2] - m[0][2] * m[2][0]),
                invDet * (m[0][2] * m[1][0] - m[0][0] * m[1][2])
            },
            {
                invDet * (m[1][0] * m[2][1] - m[1][1] * m[2][0]),
                invDet * (m[0][1] * m[2][0] - m[0][0] * m[2][1]),
                invDet * (m[0][0] * m[1][1] - m[0][1] * m[1][0])
            }
        };
    }

   
    public static double[][] matrizTranslacao(double tx, double ty) {
        return new double[][] {
            { 1, 0, tx },
            { 0, 1, ty },
            { 0, 0, 1  }
        };
    }

    
    public static double[][] matrizEscala(double sx, double sy) {
        return new double[][] {
            { sx, 0,  0 },
            { 0,  sy, 0 },
            { 0,  0,  1 }
        };
    }

    
    public static double[][] matrizEspelhamento(boolean horizontal, boolean vertical,
                                                 int largura, int altura) {
        double sx = horizontal ? -1 : 1;
        double sy = vertical   ? -1 : 1;
        double tx = horizontal ? (largura - 1) : 0;
        double ty = vertical   ? (altura - 1)  : 0;

        return new double[][] {
            { sx, 0,  tx },
            { 0,  sy, ty },
            { 0,  0,  1  }
        };
    }

   
    public static double[][] matrizRotacao(double graus, boolean isDireita, int largura, int altura) {
        double angulo = Math.toRadians(isDireita ? graus : -graus);
        double cos = Math.cos(angulo);
        double sen = Math.sin(angulo);

        double cx = largura / 2.0;
        double cy = altura / 2.0;

        return new double[][] {
            { cos, -sen, cx - cx * cos + cy * sen },
            { sen,  cos, cy - cx * sen - cy * cos },
            { 0,    0,   1                         }
        };
    }

    public static double[][] multiplicar(double[][] a, double[][] b) {
        double[][] resultado = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    resultado[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return resultado;
    }
}