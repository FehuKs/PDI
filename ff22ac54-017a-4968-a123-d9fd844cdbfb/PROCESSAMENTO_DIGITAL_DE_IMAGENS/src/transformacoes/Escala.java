package src.transformacoes;

import java.awt.image.BufferedImage;


public class Escala {


    public BufferedImage aplicarEscala(BufferedImage imagem, double fatorEscala) {
        int novaLargura = (int) (imagem.getWidth()  * fatorEscala);
        int novaAltura  = (int) (imagem.getHeight() * fatorEscala);

        double[][] matriz = TransformacaoMatricial.matrizEscala(fatorEscala, fatorEscala);

        return TransformacaoMatricial.aplicar(imagem, matriz, novaLargura, novaAltura);
    }
}

