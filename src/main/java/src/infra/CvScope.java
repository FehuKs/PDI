package src.infra;

import java.util.ArrayDeque;
import java.util.Deque;
import org.opencv.core.Mat;

/** Owns temporary native matrices for one synchronous analysis, including helper calls. */
public final class CvScope implements AutoCloseable {
    private static final ThreadLocal<Deque<CvScope>> SCOPES = ThreadLocal.withInitial(ArrayDeque::new);
    private final Deque<Mat> matrices = new ArrayDeque<>();
    private CvScope() { OpenCvRuntime.ensureLoaded(); SCOPES.get().push(this); }
    public static CvScope open() { return new CvScope(); }
    public static <T extends Mat> T track(T matrix) {
        if (!SCOPES.get().isEmpty()) SCOPES.get().peek().matrices.push(matrix);
        return matrix;
    }
    @Override public void close() {
        if (SCOPES.get().peek() != this) throw new IllegalStateException("Ordem inválida de escopos OpenCV.");
        SCOPES.get().pop();
        while (!matrices.isEmpty()) matrices.pop().release();
        if (SCOPES.get().isEmpty()) SCOPES.remove();
    }
}
