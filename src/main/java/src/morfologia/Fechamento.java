package src.morfologia;

import java.awt.image.BufferedImage;

/** Composição com o mesmo elemento estruturante 3×3 e preservação das bordas. */
public class Fechamento {
    public BufferedImage aplicarFiltro(BufferedImage image) {
        return new Erosao().aplicarFiltro(new Dilatacao().aplicarFiltro(image));
    }
}
