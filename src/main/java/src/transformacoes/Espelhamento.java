package src.transformacoes;

import java.awt.image.BufferedImage;


public class Espelhamento {

   
    public BufferedImage espelharImagem(BufferedImage imagem, boolean horizontal, boolean vertical) {
        int largura = imagem.getWidth();
        int altura  = imagem.getHeight();

        double[][] matriz = TransformacaoMatricial.matrizEspelhamento(horizontal, vertical, largura, altura);

        return TransformacaoMatricial.aplicar(imagem, matriz, largura, altura);
    }
}
