package src;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import src.afinamento.ZhangSuen;
import src.core.AnalysisResult;
import src.desafio.AnalisadorBarras;
import src.desafio.ContadorObjetos;
import src.desafio.IdentificadorPlacas;
import src.desafio.LeitorRelogio;
import src.desafio.ReconhecedorLetras;
import src.filtros.FiltrodaMediana;
import src.filtros.Grayscale;
import src.filtros.Limiarizacao;
import src.infra.ImageFiles;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/** Acceptance of the original teacher images; included in the default regression suite. */
class TeacherImagesTest {
    private record Sample(
            String file, String challenge, List<String> summaries, Map<String, String> counts) {
        @Override
        public String toString() {
            return file;
        }
    }

    static Stream<Sample> samples() {
        return Stream.of(
                new Sample(
                        "Formas 1.png",
                        "formas",
                        List.of(),
                        Map.of(
                                "vermelhos.quadrados",
                                "3",
                                "verdes.círculos",
                                "5",
                                "azuis.círculos",
                                "2")),
                new Sample(
                        "Formas 2.jpeg",
                        "formas",
                        List.of(),
                        Map.of(
                                "vermelhos.círculos",
                                "4",
                                "amarelos.círculos",
                                "1",
                                "azuis.quadrados",
                                "2")),
                new Sample(
                        "Formas 3.jpeg",
                        "formas",
                        List.of(),
                        Map.of("verdes.quadrados", "6", "amarelos.círculos", "3")),
                example("Grafico 1.jpeg", "barras", "Maior = 20 | Menor = 5"),
                example("Grafico 2.jpeg", "barras", "Maior = 7 | Menor = 7"),
                example("Letras 1.jpeg", "letras", "A, B, C"),
                example("Letras 2.jpeg", "letras", "X, Y, Z"),
                example("Letras 3.jpeg", "letras", "M, M, M"),
                example("Placa 1.jpeg", "placas", "Velocidade maxima"),
                example("Placa 2.jpeg", "placas", "Pare"),
                example("Placa 3.jpeg", "placas", "Sentido proibido"),
                example("Placa 4.jpeg", "placas", "Proibido estacionar"),
                example("Relogio 1.jpeg", "relogios", "07:31"),
                example("Relogio 2.jpeg", "relogios", "03:00", "02:59"),
                example("Relogio 3.jpeg", "relogios", "10:15"));
    }

    private static Sample example(String file, String challenge, String... summaries) {
        return new Sample(file, challenge, List.of(summaries), Map.of());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("samples")
    void originalImageMatchesExpectedResult(Sample sample) throws Exception {
        Path path = Path.of("src/test/resources/teacher-images").resolve(sample.file());
        String hash =
                HexFormat.of()
                        .formatHex(
                                MessageDigest.getInstance("SHA-256")
                                        .digest(Files.readAllBytes(path)));
        BufferedImage image = new ImageFiles().read(path);
        long start = System.nanoTime();
        AnalysisResult result = evaluate(sample.challenge(), image);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        String expected =
                sample.counts().isEmpty()
                        ? String.join(" OU ", sample.summaries())
                        : new TreeMap<>(sample.counts()).toString();
        // Machine-readable evidence, restricted to image metadata and analysis output.
        System.out.printf(
                "TEACHER_RESULT\t%s\t%s\t%s\t%d\t%d\t%s\t%s\t%s\t%s\t%d%n",
                sample.file(),
                sample.challenge(),
                hash,
                image.getWidth(),
                image.getHeight(),
                expected,
                result.status(),
                result.summary(),
                new TreeMap<>(result.details()),
                elapsedMs);
        assertAll(
                sample.file(),
                () -> assertEquals(AnalysisResult.Status.IDENTIFIED, result.status()),
                () -> {
                    if (!sample.counts().isEmpty()) {
                        assertEquals(sample.counts(), result.details());
                    } else {
                        assertTrue(
                                sample.summaries().contains(result.summary()),
                                () -> "Esperado: " + expected + "; obtido: " + result.summary());
                    }
                });
    }

    private static AnalysisResult evaluate(String challenge, BufferedImage image) {
        var gray = new Grayscale();
        var threshold = new Limiarizacao();
        return switch (challenge) {
            case "formas" -> new ContadorObjetos(new FiltrodaMediana()).evaluate(image);
            case "barras" -> new AnalisadorBarras(gray, threshold).evaluate(image);
            case "letras" ->
                    new ReconhecedorLetras(gray, threshold, new FiltrodaMediana()).evaluate(image);
            case "placas" -> new IdentificadorPlacas(gray, threshold).evaluate(image);
            case "relogios" -> new LeitorRelogio(gray, threshold, new ZhangSuen()).evaluate(image);
            default -> throw new IllegalArgumentException("Desafio desconhecido: " + challenge);
        };
    }
}
