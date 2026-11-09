package src;

import java.awt.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import src.desafio.*;
import src.filtros.*;
import src.afinamento.ZhangSuen;
import src.core.AnalysisResult.Status;

class RecognitionTest {
    private final ContadorObjetos counter=new ContadorObjetos(new FiltrodaMediana());
    private final ReconhecedorLetras letters=new ReconhecedorLetras(new Grayscale(),new Limiarizacao(),new FiltrodaMediana());
    private final IdentificadorPlacas signs=new IdentificadorPlacas(new Grayscale(),new Limiarizacao());
    private final LeitorRelogio clock=new LeitorRelogio(new Grayscale(),new Limiarizacao(),new ZhangSuen());
    private final AnalisadorBarras bars=new AnalisadorBarras(new Grayscale(),new Limiarizacao());
    @ParameterizedTest @CsvSource({"square,quadrados","circle,círculos","rectangle,retângulos","triangle,triângulos"})
    void countsShapeAndColor(String shape,String expected) {
        for(Color color:new Color[]{Color.RED,Color.YELLOW,Color.GREEN,Color.BLUE})
            assertTrue(counter.contar(Fixtures.shape(shape,color)).startsWith("1 "+expected+" "));
    }
    @ParameterizedTest @ValueSource(strings={"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"})
    void generatedUppercaseLetters(String expected) { assertEquals(expected,letters.reconhecer(Fixtures.letter(expected.charAt(0)))); }
    @Test void unknownGlyphAndSignAreNotInventedClasses() {
        assertEquals(Status.INCONCLUSIVE,letters.evaluate(Fixtures.shape("square",Color.BLACK)).status());
        assertEquals(Status.INCONCLUSIVE,signs.evaluate(Fixtures.shape("square",Color.RED)).status());
    }
    @Test void supportsStopAndSpeedSignCandidates() {
        var stop=Fixtures.canvas(220,220);Graphics2D g=stop.createGraphics();g.setColor(Color.RED);
        int[] x=new int[8],y=new int[8];for(int n=0;n<8;n++){double a=Math.toRadians(22.5+n*45);x[n]=110+(int)(80*Math.cos(a));y[n]=110+(int)(80*Math.sin(a));}
        g.fillPolygon(x,y,8);g.dispose();assertEquals("Pare",signs.identificar(stop));
        var speed=Fixtures.canvas(220,220);g=speed.createGraphics();g.setColor(Color.RED);g.setStroke(new BasicStroke(12));g.drawOval(40,40,140,140);g.dispose();
        assertEquals("Velocidade maxima",signs.identificar(speed));
    }
    @Test void emptyInputsAreExplicit() {
        var blank=Fixtures.canvas(300,300);
        assertEquals(Status.NOT_FOUND,counter.evaluate(blank).status());
        assertEquals(Status.NOT_FOUND,letters.evaluate(blank).status());assertEquals(Status.NOT_FOUND,signs.evaluate(blank).status());
        assertEquals(Status.NOT_FOUND,bars.evaluate(blank).status());assertEquals(Status.INCONCLUSIVE,clock.evaluate(blank).status());
    }
    @Test void barsWithoutCalibrationAreUnknown() {
        var result=bars.evaluate(Fixtures.bars(false));assertEquals(Status.INCONCLUSIVE,result.status());
        assertEquals("Maior = indeterminado | Menor = indeterminado",result.summary());
    }
    @Test void zeroIsNotMissingData() { assertEquals("Maior = 20 | Menor = 0",bars.analisar(Fixtures.bars("0", "20"))); }
    @Test void labelledBars() { assertEquals("Maior = 20 | Menor = 10",bars.analisar(Fixtures.bars(true))); }
    @ParameterizedTest @ValueSource(ints={3,6})
    void syntheticClock(int hour) { assertEquals(String.format("%02d:00",hour),clock.lerHorario(Fixtures.clock(hour,0))); }
    @Test void nativeTemplatesRemainValidAcrossRepeatedAnalyses() {
        for(int i=0;i<5;i++)assertEquals("A",letters.reconhecer(Fixtures.letter('A')));
    }
}
