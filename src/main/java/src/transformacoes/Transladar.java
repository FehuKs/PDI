package src.transformacoes;

import java.awt.image.BufferedImage;

/**
 * Aplica translação na imagem usando multiplicação de matrizes 3x3,
 * conforme o material de PDI (PEDRINI; SCHWARTZ, 2008).
 *
 * Matriz de translação:
 *
 *   T = [ 1  0  tx ]
 *       [ 0  1  ty ]
 *       [ 0  0  1  ]
 *
 * Para cada pixel (x, y) da imagem de origem, a posição de destino é:
 *
 *   [ xd ]   [ 1  0  tx ]   [ x ]
 *   [ yd ] = [ 0  1  ty ] . [ y ]
 *   [ 1  ]   [ 0  0  1  ]   [ 1 ]
 *
 * Resultado: xd = x + tx, yd = y + ty
 */
public class Transladar {

    /**
     * Translada a imagem de acordo com os deslocamentos fornecidos.
     *
     * @param imagem Imagem original
     * @param tx     Deslocamento horizontal em pixels
     * @param ty     Deslocamento vertical em pixels
     * @return       Imagem transladada
     */
    public BufferedImage transladarImagem(BufferedImage imagem, int tx, int ty) {
        int largura = imagem.getWidth();
        int altura  = imagem.getHeight();

        double[][] matriz = TransformacaoMatricial.matrizTranslacao(tx, ty);

        return TransformacaoMatricial.aplicar(imagem, matriz, largura, altura);
    }
}
