package src.ui;

import java.awt.*;
import java.awt.geom.*;

import javax.swing.Icon;

/** Small monochrome line icons inherit the control's foreground and disabled state. */
public final class LineIcon implements Icon {
    public enum Kind {
        OPEN,
        EXPORT,
        UNDO,
        RESET,
        SIDEBAR,
        IMAGE,
        TRANSFORM,
        FILTER,
        MORPHOLOGY,
        THINNING,
        ANALYZE,
        INFO,
        CLOSE,
        FIT,
        DOWN
    }

    private final Kind kind;
    private final int size;

    public LineIcon(Kind kind) {
        this(kind, 18);
    }

    public LineIcon(Kind kind, int size) {
        this.kind = kind;
        this.size = size;
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }

    @Override
    public void paintIcon(Component c, Graphics graphics, int x, int y) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.translate(x, y);
        g.scale(size / 24.0, size / 24.0);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(!c.isEnabled() ? Theme.DISABLED : c.getForeground());
        g.setStroke(new BasicStroke(1.65f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (kind) {
            case OPEN -> {
                line(g, 12, 4, 12, 15);
                line(g, 8, 8, 12, 4);
                line(g, 16, 8, 12, 4);
                path(g, 4, 14, 4, 20, 20, 20, 20, 14);
            }
            case EXPORT -> {
                line(g, 12, 15, 12, 4);
                line(g, 8, 11, 12, 15);
                line(g, 16, 11, 12, 15);
                path(g, 4, 15, 4, 20, 20, 20, 20, 15);
            }
            case UNDO -> {
                path(g, 9, 5, 4, 10, 9, 15);
                path(g, 4, 10, 14, 10);
                g.draw(new Arc2D.Double(9, 10, 12, 10, -90, 180, Arc2D.OPEN));
            }
            case RESET -> {
                g.draw(new Arc2D.Double(4, 4, 16, 16, 35, 295, Arc2D.OPEN));
                path(g, 19, 3, 19, 8, 14, 8);
            }
            case SIDEBAR -> {
                g.drawRoundRect(3, 4, 18, 16, 3, 3);
                line(g, 9, 4, 9, 20);
            }
            case IMAGE -> {
                g.drawRoundRect(3, 3, 18, 18, 3, 3);
                g.drawOval(7, 7, 3, 3);
                path(g, 4, 17, 10, 12, 14, 16, 17, 13, 21, 17);
            }
            case TRANSFORM -> {
                g.drawRect(6, 6, 12, 12);
                path(g, 2, 8, 2, 2, 8, 2);
                path(g, 16, 22, 22, 22, 22, 16);
            }
            case FILTER -> {
                line(g, 4, 6, 20, 6);
                line(g, 4, 12, 20, 12);
                line(g, 4, 18, 20, 18);
                g.fillOval(7, 3, 5, 5);
                g.fillOval(14, 9, 5, 5);
                g.fillOval(6, 15, 5, 5);
            }
            case MORPHOLOGY -> {
                g.drawRect(4, 4, 10, 10);
                g.drawRect(10, 10, 10, 10);
            }
            case THINNING -> {
                path(g, 5, 4, 12, 12, 19, 4);
                line(g, 12, 12, 12, 21);
            }
            case ANALYZE -> {
                g.drawOval(3, 3, 13, 13);
                line(g, 14, 14, 21, 21);
                path(g, 7, 11, 9, 8, 12, 11);
            }
            case INFO -> {
                g.drawOval(3, 3, 18, 18);
                line(g, 12, 11, 12, 17);
                g.fillOval(11, 6, 2, 2);
            }
            case CLOSE -> {
                line(g, 6, 6, 18, 18);
                line(g, 18, 6, 6, 18);
            }
            case DOWN -> path(g, 7, 9, 12, 14, 17, 9);
            case FIT -> {
                path(g, 8, 3, 3, 3, 3, 8);
                path(g, 16, 3, 21, 3, 21, 8);
                path(g, 3, 16, 3, 21, 8, 21);
                path(g, 16, 21, 21, 21, 21, 16);
            }
        }
        g.dispose();
    }

    private static void line(Graphics2D g, int x, int y, int a, int b) {
        g.drawLine(x, y, a, b);
    }

    private static void path(Graphics2D g, int... points) {
        Path2D p = new Path2D.Double();
        p.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2) p.lineTo(points[i], points[i + 1]);
        g.draw(p);
    }
}
