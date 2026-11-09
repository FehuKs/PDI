package src;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import src.infra.ImageFiles;
import src.filtros.*;
import src.desafio.*;
import src.afinamento.ZhangSuen;
import src.core.AnalysisResult.Status;

class ExamplesTest {
    @Test void checkedInExamplesHaveDocumentedResults() throws Exception {
        var files=new ImageFiles();var gray=new Grayscale();var threshold=new Limiarizacao();
        var counter=new ContadorObjetos(new FiltrodaMediana());
        var count=counter.evaluate(files.read(Path.of("examples/formas.png")));
        assertEquals(Status.IDENTIFIED,count.status());assertEquals(4,count.details().size());
        assertEquals("A, B, C, D",new ReconhecedorLetras(gray,threshold,new FiltrodaMediana()).reconhecer(files.read(Path.of("examples/letras.png"))));
        var clock=new LeitorRelogio(gray,threshold,new ZhangSuen());
        assertEquals("03:00",clock.lerHorario(files.read(Path.of("examples/relogio-3.png"))));
        assertEquals("06:00",clock.lerHorario(files.read(Path.of("examples/relogio-6.png"))));
        var bars=new AnalisadorBarras(gray,threshold);
        assertEquals("Maior = 20 | Menor = 10",bars.analisar(files.read(Path.of("examples/barras.png"))));
        assertEquals(Status.INCONCLUSIVE,bars.evaluate(files.read(Path.of("examples/barras-sem-escala.png"))).status());
        var signs=new IdentificadorPlacas(gray,threshold);
        assertEquals("Pare",signs.identificar(files.read(Path.of("examples/pare.png"))));
        assertEquals(Status.INCONCLUSIVE,signs.evaluate(files.read(Path.of("examples/quadrado-vermelho.png"))).status());
    }
}
