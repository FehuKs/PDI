package src;

import java.awt.image.BufferedImage;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import src.filtros.*;
import src.morfologia.*;
import src.transformacoes.*;
import src.core.Images;

class ProcessingTest {
    static Stream<Arguments> operations() {
        return Stream.of(
            Arguments.of("brilho", (UnaryOperator<BufferedImage>)(i -> new Brilho().aplicarBrilho(i,0))),
            Arguments.of("contraste", (UnaryOperator<BufferedImage>)(i -> new Contraste().aplicarContraste(i,1))),
            Arguments.of("gauss", (UnaryOperator<BufferedImage>)(i -> new FiltrodeGaus().aplicarFiltro(i))),
            Arguments.of("mediana", (UnaryOperator<BufferedImage>)(i -> new FiltrodaMediana().aplicarFiltroMediana(i))),
            Arguments.of("dilatacao", (UnaryOperator<BufferedImage>)(i -> new Dilatacao().aplicarFiltro(i))),
            Arguments.of("erosao", (UnaryOperator<BufferedImage>)(i -> new Erosao().aplicarFiltro(i))),
            Arguments.of("abertura", (UnaryOperator<BufferedImage>)(i -> new Abertura().aplicarFiltro(i))),
            Arguments.of("fechamento", (UnaryOperator<BufferedImage>)(i -> new Fechamento().aplicarFiltro(i))));
    }
    @ParameterizedTest(name="{0}: alpha and input unchanged") @MethodSource("operations")
    void preservesAlpha(String name, UnaryOperator<BufferedImage> operation) {
        var image = Fixtures.solid(BufferedImage.TYPE_INT_ARGB, 0x7f808080);
        var result = operation.apply(image);
        assertEquals(0x7f, result.getRGB(3,3)>>>24);
        assertEquals(0x7f808080, image.getRGB(3,3));assertNotSame(image,result);
    }
    @ParameterizedTest(name="{0}: gray field invariant") @MethodSource("operations")
    void preservesUniformGray(String name, UnaryOperator<BufferedImage> operation) {
        var image = Fixtures.solid(BufferedImage.TYPE_BYTE_GRAY,0xff808080);
        var result = operation.apply(image);
        for(int y=0;y<7;y++)for(int x=0;x<7;x++)assertEquals(image.getRGB(x,y),result.getRGB(x,y));
    }
    @ParameterizedTest @ValueSource(ints={90,180,270,360,-90})
    void rotationsPreserveAllMarkers(int angle) {
        var image = new BufferedImage(3,2,BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<2;y++)for(int x=0;x<3;x++)image.setRGB(x,y,0xff000000|1+x+y*3);
        var result = new Rotacionar().rotacionarImagem(image,angle,true);
        java.util.Set<Integer> colors = new java.util.HashSet<>();
        for(int y=0;y<result.getHeight();y++)for(int x=0;x<result.getWidth();x++)colors.add(result.getRGB(x,y));
        for(int v=1;v<=6;v++)assertTrue(colors.contains(0xff000000|v));assertEquals(6,colors.size());
    }
    @Test void geometryIdentityMirrorAndTranslation() {
        var image=Fixtures.solid(BufferedImage.TYPE_INT_RGB,0xff000000);image.setRGB(1,3,0xff123456);
        var mirror=new Espelhamento().espelharImagem(image,true,false);assertEquals(0xff123456,mirror.getRGB(5,3));
        assertEquals(image.getRGB(1,3),new Espelhamento().espelharImagem(mirror,true,false).getRGB(1,3));
        assertEquals(0xff123456,new Transladar().transladarImagem(image,2,-1).getRGB(3,2));
    }
    @Test void scaleMinimumAndBudgets() {
        var image=new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);
        assertEquals(1,new Escala().aplicarEscala(image,.5).getWidth());
        assertEquals(1,new Escala().aplicarEscala(image,1e-300).getWidth());
        for(double factor:new double[]{0,-1,Double.NaN,Double.POSITIVE_INFINITY,1e30})
            assertThrows(IllegalArgumentException.class,()->new Escala().aplicarEscala(image,factor));
        assertThrows(IllegalArgumentException.class,()->Images.dimensions(5000,5000));
    }
    @Test void grayMeanThresholdAndSaturation() {
        var image=Fixtures.solid(BufferedImage.TYPE_INT_ARGB,0x7fff0000);
        assertEquals(0x7f555555,new Grayscale().aplicarGrayscale(image).getRGB(3,3));
        assertEquals(0x7fffffff,new Limiarizacao().aplicarLimiarizacao(image,85).getRGB(3,3));
        assertEquals(0x7f000000,new Limiarizacao().aplicarLimiarizacao(image,86).getRGB(3,3));
        assertEquals(0x7fffffff,new Brilho().aplicarBrilho(image,255).getRGB(3,3));
        assertThrows(IllegalArgumentException.class,()->new Contraste().aplicarContraste(image,Float.NaN));
        assertThrows(IllegalArgumentException.class,()->new Marrhildreth(Double.NaN,15));
    }
    @Test void morphologyAndThinning() {
        var impulse=Fixtures.solid(BufferedImage.TYPE_INT_RGB,0xff000000);impulse.setRGB(3,3,0xffffffff);
        var dilated=new Dilatacao().aplicarFiltro(impulse);int white=0;
        for(int y=0;y<7;y++)for(int x=0;x<7;x++)if(dilated.getRGB(x,y)==0xffffffff)white++;
        assertEquals(9,white);assertEquals(0xff000000,new Abertura().aplicarFiltro(impulse).getRGB(3,3));
        var black=Fixtures.solid(BufferedImage.TYPE_INT_RGB,0xffffffff);black.setRGB(3,3,0xff000000);
        assertEquals(0xff000000,new src.afinamento.ZhangSuen().aplicarThinning(black).getRGB(3,3));
        assertEquals(0xff000000,new Marrhildreth(1.4,15).aplicarFiltro(Fixtures.solid(BufferedImage.TYPE_INT_RGB,0xff808080)).getRGB(3,3));
    }
}
