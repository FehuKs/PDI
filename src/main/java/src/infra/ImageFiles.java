package src.infra;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.util.Iterator;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import src.core.Images;

public final class ImageFiles {
    private static final long MAX_FILE_BYTES = 64L * 1024 * 1024;

    public BufferedImage read(Path path) throws IOException {
        if (!Files.isRegularFile(path) || Files.size(path) > MAX_FILE_BYTES)
            throw new IOException("Selecione uma imagem de até 64 MiB.");
        try (ImageInputStream input = ImageIO.createImageInputStream(path.toFile())) {
            if (input == null) throw new IOException("Não foi possível ler o arquivo.");
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IOException("Formato de imagem não suportado.");
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                Images.dimensions(reader.getWidth(0), reader.getHeight(0));
                return Images.require(reader.read(0));
            } finally { reader.dispose(); }
        }
    }

    /** A failed encoding never truncates a previously saved destination. */
    public void writePng(BufferedImage image, Path destination) throws IOException {
        Images.require(image);
        Path absolute = destination.toAbsolutePath();
        Path temporary = Files.createTempFile(absolute.getParent(), ".pdi-", ".png");
        try {
            if (!ImageIO.write(image, "png", temporary.toFile())) throw new IOException("Codificador PNG indisponível.");
            try { Files.move(temporary, absolute, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException ex) { Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }
}
