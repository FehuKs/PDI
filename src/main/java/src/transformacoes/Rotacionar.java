package src.transformacoes;

import java.awt.image.BufferedImage;


public class Rotacionar {


    public BufferedImage rotacionarImagem(BufferedImage imagem, int graus, boolean isDireita) {
        int larguraOrigem = imagem.getWidth();
        int alturaOrigem  = imagem.getHeight();

        double angulo = Math.toRadians(isDireita ? graus : -graus);
        double cos = Math.abs(Math.cos(angulo));
        double sen = Math.abs(Math.sin(angulo));

        // Calcula o tamanho do canvas de destino para não cortar a imagem
        // após a rotação para qualquer ângulo
        int novaLargura = (int) Math.ceil(larguraOrigem * cos + alturaOrigem * sen - 1e-9);
        int novaAltura  = (int) Math.ceil(larguraOrigem * sen + alturaOrigem * cos - 1e-9);

        // Centro da imagem original e do novo canvas
        double cxOrigem = (larguraOrigem - 1) / 2.0;
        double cyOrigem = (alturaOrigem - 1) / 2.0;
        double cxDestino = (novaLargura - 1) / 2.0;
        double cyDestino = (novaAltura - 1) / 2.0;

        double cosA = Math.cos(angulo);
        double senA = Math.sin(angulo);


        double[][] matriz = {
            { cosA, -senA, cxDestino - cxOrigem * cosA + cyOrigem * senA },
            { senA,  cosA, cyDestino - cxOrigem * senA - cyOrigem * cosA },
            { 0,     0,    1                                               }
        };

        return TransformacaoMatricial.aplicar(imagem, matriz, novaLargura, novaAltura);
    }
}
