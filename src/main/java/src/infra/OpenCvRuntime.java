package src.infra;

/** Loads the matching native library bundled with the pinned OpenCV distribution. */
public final class OpenCvRuntime {
    private static boolean loaded;
    private OpenCvRuntime() {}
    public static synchronized void ensureLoaded() {
        if (!loaded) {
            nu.pattern.OpenCV.loadLocally();
            loaded = true;
        }
    }
}
