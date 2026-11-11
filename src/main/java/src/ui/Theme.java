package src.ui;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSliderUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/** Shared visual tokens and standard Swing defaults; no runtime font or theme downloads. */
public final class Theme {
    public static final Color PAPER = new Color(0xffffff);
    public static final Color SIDEBAR = new Color(0xf7f7f5);
    public static final Color CANVAS = new Color(0xf4f4f1);
    public static final Color TEXT = new Color(0x292925);
    public static final Color MUTED = new Color(0x666660);
    public static final Color DISABLED = new Color(0x787872);
    public static final Color BORDER = new Color(0xe3e3de);
    public static final Color INPUT_BORDER = new Color(0x8b8b83);
    public static final Color HOVER = new Color(0xefefeb);
    public static final Color ACCENT = new Color(0x315c70);
    public static final Color ACCENT_SOFT = new Color(0xe6eef1);
    public static final int XS = 4, SM = 8, MD = 12, LG = 16, XL = 24, XXL = 32;
    public static final int RADIUS = 8, CONTROL_HEIGHT = 36;
    public static final int COMPACT_CONTROL_HEIGHT = 32, DIALOG_WIDTH = 480;
    public static final int SIDEBAR_WIDTH = 248, RAIL_WIDTH = 64, COMPACT_AT = 1020;
    public static final int STACK_AT_WIDTH = 760, STACK_MIN_HEIGHT = 680;
    public static final Font TITLE = font(Font.BOLD, 22);
    public static final Font SECTION = font(Font.BOLD, 14);
    public static final Font BODY = font(Font.PLAIN, 14);
    public static final Font SMALL = font(Font.PLAIN, 12);
    private static boolean installed;

    private Theme() {}

    private static Font font(int style, int size) {
        return new Font(Font.SANS_SERIF, style, size);
    }

    public static synchronized void install() {
        if (installed) return;
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException
                | ClassNotFoundException
                | InstantiationException
                | IllegalAccessException ex) {
            throw new IllegalStateException("Não foi possível preparar a interface.", ex);
        }
        for (String key :
                new String[] {
                    "Label.font",
                    "Button.font",
                    "ToggleButton.font",
                    "TextField.font",
                    "TextArea.font",
                    "Menu.font",
                    "MenuItem.font",
                    "CheckBox.font",
                    "ComboBox.font",
                    "ToolTip.font"
                }) UIManager.put(key, BODY);
        for (String key :
                new String[] {
                    "Panel.background",
                    "TextField.background",
                    "TextArea.background",
                    "MenuItem.background",
                    "Menu.background",
                    "PopupMenu.background",
                    "CheckBox.background",
                    "ComboBox.background"
                }) UIManager.put(key, PAPER);
        for (String key :
                new String[] {
                    "Label.foreground",
                    "Button.foreground",
                    "TextField.foreground",
                    "TextArea.foreground",
                    "Menu.foreground",
                    "MenuItem.foreground",
                    "CheckBox.foreground",
                    "ComboBox.foreground"
                }) UIManager.put(key, TEXT);
        UIManager.getLookAndFeelDefaults().put("MenuBar.gradient", null);
        UIManager.getLookAndFeelDefaults().put("CheckBox.gradient", null);
        UIManager.put("ComboBox.selectionBackground", ACCENT_SOFT);
        UIManager.put("ComboBox.selectionForeground", TEXT);
        UIManager.put("List.selectionBackground", ACCENT_SOFT);
        UIManager.put("List.selectionForeground", TEXT);
        UIManager.put("Menu.selectionBackground", HOVER);
        UIManager.put("Menu.selectionForeground", TEXT);
        UIManager.put("MenuItem.selectionBackground", ACCENT_SOFT);
        UIManager.put("MenuItem.selectionForeground", ACCENT);
        UIManager.put("MenuItem.disabledForeground", DISABLED);
        UIManager.put("TextField.selectionBackground", ACCENT_SOFT);
        UIManager.put("TextField.selectionForeground", TEXT);
        UIManager.put("TextField.caretForeground", ACCENT);
        UIManager.put("Button.disabledText", DISABLED);
        UIManager.put("ProgressBar.foreground", ACCENT);
        UIManager.put("ProgressBar.background", HOVER);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.thumb", BORDER);
        UIManager.put("ScrollBar.track", SIDEBAR);
        UIManager.put("ToolTip.background", TEXT);
        UIManager.put("ToolTip.foreground", PAPER);
        UIManager.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        installed = true;
    }

    public static JPanel panel(LayoutManager layout, Color background) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(background);
        return panel;
    }

    public static JLabel label(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        smooth(label);
        return label;
    }

    public static JTextArea text(String value) {
        JTextArea text = new JTextArea(value);
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setFont(BODY);
        text.setForeground(TEXT);
        text.setOpaque(false);
        text.setBorder(null);
        smooth(text);
        return text;
    }

    public static JScrollPane scroll(Component content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(content.getBackground());
        scroll.getVerticalScrollBar().setUnitIncrement(XL);
        for (JScrollBar bar :
                new JScrollBar[] {scroll.getVerticalScrollBar(), scroll.getHorizontalScrollBar()}) {
            bar.setUI(
                    new BasicScrollBarUI() {
                        @Override
                        protected void configureScrollBarColors() {
                            thumbColor = BORDER;
                            trackColor = SIDEBAR;
                        }

                        private JButton emptyButton() {
                            JButton button = new JButton();
                            button.setPreferredSize(new Dimension(0, 0));
                            button.setFocusable(false);
                            return button;
                        }

                        @Override
                        protected JButton createDecreaseButton(int direction) {
                            return emptyButton();
                        }

                        @Override
                        protected JButton createIncreaseButton(int direction) {
                            return emptyButton();
                        }

                        @Override
                        protected void paintThumb(
                                Graphics graphics, JComponent c, Rectangle bounds) {
                            if (!c.isEnabled() || bounds.isEmpty()) return;
                            Graphics2D g = (Graphics2D) graphics.create();
                            g.setColor(INPUT_BORDER);
                            g.fillRoundRect(
                                    bounds.x + 2,
                                    bounds.y + 2,
                                    Math.max(2, bounds.width - 4),
                                    Math.max(2, bounds.height - 4),
                                    RADIUS,
                                    RADIUS);
                            g.dispose();
                        }
                    });
        }
        return scroll;
    }

    public static String shortcutHint(String key) {
        return (System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("mac")
                        ? "Cmd"
                        : "Ctrl")
                + "+"
                + key;
    }

    public static void smooth(JComponent component) {
        component.putClientProperty(
                RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    public static void splitter(JSplitPane split) {
        split.setUI(
                new BasicSplitPaneUI() {
                    @Override
                    public BasicSplitPaneDivider createDefaultDivider() {
                        return new BasicSplitPaneDivider(this) {
                            @Override
                            public void paint(Graphics g) {
                                g.setColor(CANVAS);
                                g.fillRect(0, 0, getWidth(), getHeight());
                                g.setColor(BORDER);
                                if (split.getOrientation() == JSplitPane.HORIZONTAL_SPLIT)
                                    g.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
                                else g.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
                            }
                        };
                    }
                });
    }

    public static void field(JComponent field) {
        smooth(field);
        if (field instanceof JComboBox<?> combo) {
            combo.setUI(
                    new BasicComboBoxUI() {
                        @Override
                        protected JButton createArrowButton() {
                            JButton arrow = new JButton(new LineIcon(LineIcon.Kind.DOWN));
                            arrow.setForeground(TEXT);
                            arrow.setBorder(BorderFactory.createEmptyBorder(XS, XS, XS, XS));
                            arrow.setContentAreaFilled(false);
                            arrow.setFocusable(false);
                            arrow.getAccessibleContext().setAccessibleName("Mostrar opções");
                            return arrow;
                        }

                        @Override
                        public void paintCurrentValueBackground(
                                Graphics graphics, Rectangle bounds, boolean focus) {
                            graphics.setColor(PAPER);
                            graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
                        }
                    });
        }
        field.setFont(BODY);
        field.setForeground(TEXT);
        field.setBackground(PAPER);
        field.setBorder(
                new AbstractBorder() {
                    @Override
                    public Insets getBorderInsets(Component c) {
                        return new Insets(SM, MD, SM, MD);
                    }

                    @Override
                    public void paintBorder(
                            Component c, Graphics graphics, int x, int y, int width, int height) {
                        Graphics2D g = (Graphics2D) graphics.create();
                        g.setRenderingHint(
                                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g.setColor(c.hasFocus() ? ACCENT : INPUT_BORDER);
                        g.setStroke(new BasicStroke(c.hasFocus() ? 2f : 1f));
                        g.drawRoundRect(x + 1, y + 1, width - 3, height - 3, RADIUS, RADIUS);
                        g.dispose();
                    }
                });
        field.setPreferredSize(new Dimension(220, CONTROL_HEIGHT));
        field.addFocusListener(
                new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        field.repaint();
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        field.repaint();
                    }
                });
    }

    public static void slider(JSlider slider) {
        slider.setOpaque(false);
        slider.setForeground(ACCENT);
        slider.setUI(
                new BasicSliderUI(slider) {
                    @Override
                    public void paintThumb(Graphics graphics) {
                        Graphics2D g = (Graphics2D) graphics.create();
                        g.setRenderingHint(
                                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g.setColor(slider.isEnabled() ? ACCENT : DISABLED);
                        g.fillOval(
                                thumbRect.x + 1,
                                thumbRect.y + 2,
                                thumbRect.width - 2,
                                thumbRect.height - 4);
                        g.dispose();
                    }

                    @Override
                    public void paintFocus(Graphics graphics) {
                        graphics.setColor(ACCENT);
                        graphics.drawRect(
                                focusRect.x,
                                focusRect.y,
                                focusRect.width - 1,
                                focusRect.height - 1);
                    }
                });
    }
}
