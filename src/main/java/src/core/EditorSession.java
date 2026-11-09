package src.core;

import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.UnaryOperator;

/** Images owned by this session are read-only to callers. Coordinate access through one UI worker. */
public final class EditorSession {
    private static final int MAX_HISTORY = 20;
    private static final long MAX_HISTORY_BYTES = 128_000_000;
    private final Deque<BufferedImage> history = new ArrayDeque<>();
    private BufferedImage original;
    private BufferedImage current;
    private long historyBytes;

    public void load(BufferedImage image) {
        BufferedImage validated = Images.copy(image);
        original = validated;
        current = validated;
        clearHistory();
    }

    public BufferedImage apply(UnaryOperator<BufferedImage> operation) {
        requireImage();
        // A failing or mutating operation cannot corrupt the current image or its history.
        BufferedImage result = Images.require(Objects.requireNonNull(operation).apply(Images.copy(current)));
        history.push(current);
        historyBytes += bytes(current);
        while (history.size() > MAX_HISTORY || historyBytes > MAX_HISTORY_BYTES) {
            historyBytes -= bytes(history.removeLast());
        }
        current = result;
        return result;
    }

    public BufferedImage undo() {
        requireImage();
        if (!history.isEmpty()) {
            current = history.pop();
            historyBytes -= bytes(current);
        }
        return current;
    }

    public BufferedImage reset() {
        requireImage();
        current = original;
        clearHistory();
        return current;
    }

    public BufferedImage current() { requireImage(); return current; }
    public BufferedImage original() { requireImage(); return original; }
    public boolean hasImage() { return original != null; }
    public boolean canUndo() { return !history.isEmpty(); }
    public int historySize() { return history.size(); }
    private void requireImage() { if (!hasImage()) throw new IllegalStateException("Abra uma imagem primeiro."); }
    private void clearHistory() { history.clear(); historyBytes = 0; }
    private static long bytes(BufferedImage image) { return (long) image.getWidth() * image.getHeight() * 4; }
}
