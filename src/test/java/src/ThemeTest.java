package src;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import src.ui.Theme;

import java.awt.Color;

class ThemeTest {
    @Test
    void textAndFocusHaveSufficientContrastOnActualSurfaces() {
        for (Color background : new Color[] {Theme.PAPER, Theme.SIDEBAR}) {
            assertTrue(contrast(Theme.TEXT, background) >= 4.5, "Primary text");
            assertTrue(contrast(Theme.MUTED, background) >= 4.5, "Secondary text");
        }
        assertTrue(contrast(Theme.PAPER, Theme.ACCENT) >= 4.5, "Primary action text");
        assertTrue(contrast(Theme.ACCENT, Theme.ACCENT_SOFT) >= 4.5, "Selected navigation");
        assertTrue(contrast(Theme.ACCENT, Theme.PAPER) >= 3, "Keyboard focus");
        assertTrue(contrast(Theme.INPUT_BORDER, Theme.PAPER) >= 3, "Input boundaries");
    }

    private static double contrast(Color a, Color b) {
        double x = luminance(a), y = luminance(b);
        return (Math.max(x, y) + .05) / (Math.min(x, y) + .05);
    }

    private static double luminance(Color c) {
        return .2126 * linear(c.getRed())
                + .7152 * linear(c.getGreen())
                + .0722 * linear(c.getBlue());
    }

    private static double linear(int channel) {
        double value = channel / 255.0;
        return value <= .04045 ? value / 12.92 : Math.pow((value + .055) / 1.055, 2.4);
    }
}
