package src;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import src.core.EditorSession;
import src.infra.ImageFiles;
import src.filtros.Brilho;

class SessionFilesTest {
    @TempDir Path dir;
    @Test void failureIsAtomicEvenWhenOperationMutatesInput() {
        var session=new EditorSession();var original=Fixtures.solid(BufferedImage.TYPE_INT_RGB,0xff808080);session.load(original);
        assertThrows(IllegalStateException.class,()->session.apply(image->{image.setRGB(0,0,0);throw new IllegalStateException("failure");}));
        assertEquals(0xff808080,session.current().getRGB(0,0));assertFalse(session.canUndo());
        assertThrows(NullPointerException.class,()->session.apply(image->null));assertEquals(0,session.historySize());
    }
    @Test void editingHistoryAndReset() {
        var session=new EditorSession();session.load(Fixtures.solid(BufferedImage.TYPE_INT_RGB,0xff808080));
        session.apply(image->new Brilho().aplicarBrilho(image,10));assertEquals(0xff8a8a8a,session.current().getRGB(3,3));
        assertEquals(0xff808080,session.undo().getRGB(3,3));assertFalse(session.canUndo());
        for(int n=0;n<25;n++)session.apply(image->new Brilho().aplicarBrilho(image,1));assertEquals(20,session.historySize());
        assertEquals(0xff808080,session.reset().getRGB(3,3));assertEquals(0,session.historySize());
    }
    @Test void pngRoundtripAndInvalidLoadPreserveSession() throws Exception {
        var files=new ImageFiles();var path=dir.resolve("result.png");var session=new EditorSession();
        session.load(Fixtures.solid(BufferedImage.TYPE_INT_ARGB,0x7f123456));files.writePng(session.current(),path);
        assertEquals(0x7f123456,files.read(path).getRGB(3,3));
        var bad=dir.resolve("not-an-image.txt");Files.writeString(bad,"not an image");
        assertThrows(java.io.IOException.class,()->session.load(files.read(bad)));assertEquals(0x7f123456,session.current().getRGB(3,3));
        try(var entries=Files.list(dir)){assertEquals(1,entries.filter(p->p.getFileName().toString().endsWith(".png")).count());}
    }
}
