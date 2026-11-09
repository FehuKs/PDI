package src.core;

import java.awt.image.BufferedImage;
import java.util.Objects;

/** Pixel budgets are checked before allocating derived images. */
public final class Images {
    public static final long MAX_PIXELS = 16_000_000;
    private Images() {}

    public static BufferedImage require(BufferedImage image) {
        Objects.requireNonNull(image, "Imagem obrigatória.");
        dimensions(image.getWidth(), image.getHeight());
        return image;
    }

    public static void dimensions(long width, long height) {
        if (width < 1 || height < 1 || width > MAX_PIXELS || height > MAX_PIXELS
                || width * height > MAX_PIXELS) {
            throw new IllegalArgumentException("A imagem deve ter entre 1 e 16 milhões de pixels.");
        }
    }

    /** Independent RGB channels prevent quantization between channel writes on gray/indexed images. */
    public static BufferedImage like(BufferedImage source) {
        require(source);
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(),
                source.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        if (source.getColorModel().hasAlpha()) {
            for (int y = 0; y < source.getHeight(); y++)
                for (int x = 0; x < source.getWidth(); x++)
                    result.setRGB(x, y, source.getRGB(x, y) & 0xff000000);
        }
        return result;
    }

    public static BufferedImage copy(BufferedImage source) {
        BufferedImage copy = like(source);
        copy.setRGB(0, 0, source.getWidth(), source.getHeight(),
                source.getRGB(0, 0, source.getWidth(), source.getHeight(), null, 0, source.getWidth()),
                0, source.getWidth());
        return copy;
    }
}
