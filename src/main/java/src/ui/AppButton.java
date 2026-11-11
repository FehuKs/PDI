package src.ui;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Standard button semantics, with flat surfaces and a visible keyboard focus ring. */
public class AppButton extends JButton {
    public enum Style {
        PRIMARY,
        SECONDARY,
        GHOST,
        NAVIGATION,
        TOOL
    }

    private final Style style;

    public AppButton(String text, LineIcon.Kind icon, Style style) {
        super(text, icon == null ? null : new LineIcon(icon));
        this.style = style;
        Theme.smooth(this);
        setFont(Theme.BODY);
        setIconTextGap(Theme.SM);
        setMargin(new Insets(0, 0, 0, 0));
        setBorder(
                new EmptyBorder(
                        style == Style.TOOL ? Theme.XS : Theme.SM,
                        Theme.MD,
                        style == Style.TOOL ? Theme.XS : Theme.SM,
                        Theme.MD));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setHorizontalAlignment(style == Style.NAVIGATION ? LEFT : CENTER);
        setForeground(style == Style.PRIMARY ? Theme.PAPER : Theme.TEXT);
        setRolloverEnabled(true);
        getModel().addChangeListener(e -> repaint());
        if (text != null && !text.isBlank()) getAccessibleContext().setAccessibleName(text);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.height =
                Math.max(
                        size.height,
                        style == Style.TOOL ? Theme.COMPACT_CONTROL_HEIGHT : Theme.CONTROL_HEIGHT);
        return size;
    }

    @Override
    public Color getForeground() {
        if (style == null) return super.getForeground();
        if (!isEnabled()) return Theme.DISABLED;
        if (style == Style.PRIMARY) return Theme.PAPER;
        return isSelected() ? Theme.ACCENT : Theme.TEXT;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean hover = isEnabled() && (getModel().isRollover() || getModel().isPressed());
        Color bg =
                style == Style.PRIMARY
                        ? Theme.ACCENT
                        : style == Style.SECONDARY ? Theme.PAPER : null;
        if (style == Style.NAVIGATION && isSelected()) bg = Theme.ACCENT_SOFT;
        if (hover) bg = style == Style.PRIMARY ? Theme.ACCENT.darker() : Theme.HOVER;
        if (isEnabled() && getModel().isPressed() && getModel().isArmed())
            bg = style == Style.PRIMARY ? Theme.ACCENT.darker().darker() : Theme.BORDER;
        if (!isEnabled() && style == Style.PRIMARY) bg = Theme.HOVER;
        if (bg != null) {
            g.setColor(bg);
            g.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, Theme.RADIUS, Theme.RADIUS);
        }
        if (style == Style.SECONDARY) {
            g.setColor(Theme.BORDER);
            g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, Theme.RADIUS, Theme.RADIUS);
        }
        if (isFocusOwner()) {
            g.setColor(style == Style.PRIMARY && isEnabled() ? Theme.PAPER : Theme.ACCENT);
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, Theme.RADIUS, Theme.RADIUS);
        }
        g.dispose();
        super.paintComponent(graphics);
    }
}
