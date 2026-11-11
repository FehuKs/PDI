package src.ui;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

/** Fit-to-area and pixel-size display only; never resamples the edited image. */
public final class ImageCanvas extends JPanel implements Scrollable {
    private BufferedImage image;
    private boolean fit = true;

    public ImageCanvas(String accessibleName) {
        setBackground(Theme.CANVAS);
        setName(accessibleName);
        getAccessibleContext().setAccessibleName(accessibleName);
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        revalidate();
        repaint();
    }

    public void setFit(boolean fit) {
        this.fit = fit;
        revalidate();
        repaint();
    }

    public BufferedImage image() {
        return image;
    }

    @Override
    public Dimension getPreferredSize() {
        return !fit && image != null
                ? new Dimension(image.getWidth() + Theme.XXL, image.getHeight() + Theme.XXL)
                : new Dimension(160, 160);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (image == null || getWidth() < 1 || getHeight() < 1) return;
        double factor =
                fit
                        ? Math.min(
                                (getWidth() - Theme.XXL) / (double) image.getWidth(),
                                (getHeight() - Theme.XXL) / (double) image.getHeight())
                        : 1;
        factor = Math.max(.001, factor);
        int w = Math.max(1, (int) Math.round(image.getWidth() * factor)),
                h = Math.max(1, (int) Math.round(image.getHeight() * factor));
        int x = Math.max(Theme.LG, (getWidth() - w) / 2),
                y = Math.max(Theme.LG, (getHeight() - h) / 2);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                fit
                        ? RenderingHints.VALUE_INTERPOLATION_BILINEAR
                        : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setColor(Theme.PAPER);
        g.fillRect(x, y, w, h);
        g.drawImage(image, x, y, w, h, null);
        g.dispose();
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(240, 240);
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) {
        return Theme.XL;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) {
        return Math.max(
                Theme.XL, (orientation == SwingConstants.VERTICAL ? r.height : r.width) - Theme.XL);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return fit;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return fit;
    }
}
