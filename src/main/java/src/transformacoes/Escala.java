package src.transformacoes;

import src.core.Images;

import java.awt.image.BufferedImage;


public class Escala {


    public BufferedImage aplicarEscala(BufferedImage imagem, double fatorEscala) {
        Images.require(imagem);
        if (!Double.isFinite(fatorEscala) || fatorEscala <= 0) throw new IllegalArgumentException("Escala deve ser positiva e finita.");
        double w = Math.max(1, Math.floor(imagem.getWidth() * fatorEscala));
        double h = Math.max(1, Math.floor(imagem.getHeight() * fatorEscala));
        if (w > Images.MAX_PIXELS || h > Images.MAX_PIXELS) throw new IllegalArgumentException("Escala excede o limite de pixels.");
        Images.dimensions((long) w, (long) h);
        int novaLargura = (int) w;
        int novaAltura = (int) h;

        if (novaLargura == 1 && novaAltura == 1) {
            BufferedImage pixel = new BufferedImage(1, 1, imagem.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
            pixel.setRGB(0, 0, imagem.getRGB(0, 0));
            return pixel;
        }
        double[][] matriz = TransformacaoMatricial.matrizEscala(fatorEscala, fatorEscala);

        return TransformacaoMatricial.aplicar(imagem, matriz, novaLargura, novaAltura);
    }
}

