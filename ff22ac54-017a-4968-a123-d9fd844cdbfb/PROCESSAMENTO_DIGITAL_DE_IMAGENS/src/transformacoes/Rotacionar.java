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
        int novaLargura = (int) Math.round(larguraOrigem * cos + alturaOrigem * sen);
        int novaAltura  = (int) Math.round(larguraOrigem * sen + alturaOrigem * cos);

        // Centro da imagem original e do novo canvas
        double cxOrigem = larguraOrigem / 2.0;
        double cyOrigem = alturaOrigem  / 2.0;
        double cxDestino = novaLargura  / 2.0;
        double cyDestino = novaAltura   / 2.0;

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
