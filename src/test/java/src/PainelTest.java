package src;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

import src.ui.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import javax.imageio.ImageIO;
import javax.swing.*;

@EnabledIfSystemProperty(named = "java.awt.headless", matches = "false")
class PainelTest {
    @TempDir Path dir;

    @Test
    void desktopEditingAndInlineAnalysis() throws Exception {
        Path path = dir.resolve("quadrado.png");
        ImageIO.write(Fixtures.shape("square", Color.RED), "png", path.toFile());
        Painel app = open(1240, 820);
        try {
            edt(
                    () -> {
                        assertFalse(button(app, "action.save").isEnabled());
                        assertFalse(button(app, "action.undo").isEnabled());
                        assertTrue(named(app, "workspace.empty").isShowing());
                    });
            capture(app, "empty");
            edt(
                    () -> {
                        app.carregarImagem(path);
                        assertFalse(app.getJMenuBar().getMenu(2).isEnabled());
                        assertTrue(
                                ((JProgressBar) named(app, "workspace.progress"))
                                        .isIndeterminate());
                    });
            await(
                    () ->
                            status(app).contains("0 operações")
                                    && label(app, "workspace.document")
                                            .getText()
                                            .equals("quadrado.png"));
            edt(() -> app.getJMenuBar().getMenu(2).getItem(0).doClick());
            await(() -> status(app).contains("1 operação"));
            edt(
                    () ->
                            assertEquals(
                                    0xff555555,
                                    ((ImageCanvas) named(app, "Imagem resultante"))
                                            .image()
                                            .getRGB(80, 60)));
            capture(app, "comparison");
            edt(() -> button(app, "action.undo").doClick());
            await(() -> status(app).contains("0 operações"));
            edt(() -> button(app, "action.reset").doClick());
            edt(() -> app.getJMenuBar().getMenu(5).getItem(1).doClick());
            await(
                    () ->
                            named(app, "workspace.feedback").isVisible()
                                    && ((JTextArea) named(app, "feedback.message"))
                                            .getText()
                                            .contains("quadrados vermelhos"));
            assertNoParameterDialog();
            capture(app, "analysis");
            edt(() -> button(app, "feedback.dismiss").doClick());
            Path bad = dir.resolve("invalid.txt");
            Files.writeString(bad, "not an image");
            edt(() -> app.carregarImagem(bad));
            await(
                    () ->
                            named(app, "workspace.feedback").isVisible()
                                    && ((JTextArea) named(app, "feedback.message"))
                                            .getText()
                                            .contains("não suportado"));
            edt(
                    () -> {
                        assertEquals("quadrado.png", label(app, "workspace.document").getText());
                        assertEquals(
                                0xffff0000,
                                ((ImageCanvas) named(app, "Imagem original"))
                                        .image()
                                        .getRGB(80, 60));
                    });
            capture(app, "error");
        } finally {
            edt(app::dispose);
        }
    }

    @Test
    void keyboardValidationAndResponsiveLayouts() throws Exception {
        Painel app = open(1240, 820);
        try {
            edt(() -> app.carregarImagem(Path.of("examples/formas.png")));
            await(() -> status(app).contains("0 operações"));
            edt(() -> button(app, "action.save").requestFocusInWindow());
            await(() -> button(app, "action.save").isFocusOwner());
            edt(
                    () -> {
                        AbstractButton save = button(app, "action.save");
                        BufferedImage rendered =
                                new BufferedImage(
                                        save.getWidth(),
                                        save.getHeight(),
                                        BufferedImage.TYPE_INT_RGB);
                        Graphics2D graphics = rendered.createGraphics();
                        save.paint(graphics);
                        graphics.dispose();
                        assertEquals(
                                Theme.PAPER.getRGB(),
                                rendered.getRGB(save.getWidth() / 2, 2),
                                "Primary focus is visible against its fill");
                    });
            SwingUtilities.invokeLater(() -> app.getJMenuBar().getMenu(1).getItem(0).doClick());
            await(() -> parameterDialog() != null);
            JDialog dialog = parameterDialog();
            edt(
                    () -> {
                        JTextField angle = (JTextField) named(dialog, "parameter.angle");
                        angle.setText("abc");
                        dialog.getRootPane().getDefaultButton().doClick();
                        JTextArea error = (JTextArea) named(dialog, "form.error");
                        assertTrue(error.isVisible());
                        assertFalse(error.getText().isBlank());
                        assertTrue(dialog.isShowing());
                        assertTrue(status(app).contains("0 operações"));
                    });
            capture(dialog, "validation");
            edt(
                    () -> {
                        JTextField angle = (JTextField) named(dialog, "parameter.angle");
                        angle.setText("90");
                        angle.requestFocusInWindow();
                    });
            Robot robot = new Robot();
            robot.waitForIdle();
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);
            await(() -> parameterDialog() == null && status(app).contains("1 operação"));
            edt(() -> button(app, "action.reset").doClick());
            for (Dimension size :
                    List.of(
                            new Dimension(1440, 900),
                            new Dimension(800, 900),
                            new Dimension(800, 600),
                            new Dimension(760, 520))) {
                edt(
                        () -> {
                            app.setSize(size);
                            app.validate();
                        });
                robot.waitForIdle();
                edt(
                        () -> {
                            Component save = named(app, "action.save");
                            assertTrue(save.isShowing());
                            assertTrue(save.getWidth() >= 100);
                            ToolSidebar sidebar = (ToolSidebar) named(app, "sidebar");
                            assertEquals(size.width < Theme.COMPACT_AT, sidebar.isCompact());
                            JSplitPane split = (JSplitPane) named(app, "workspace.comparison");
                            assertTrue(split.getLeftComponent().getHeight() > 50);
                            assertTrue(split.getRightComponent().getHeight() > 50);
                            assertTrue(split.getWidth() > 300);
                            assertEquals(6, app.getJMenuBar().getMenuCount());
                            int tools = 0;
                            for (int i = 1; i < 6; i++)
                                tools += app.getJMenuBar().getMenu(i).getItemCount();
                            assertEquals(23, tools);
                        });
                capture(app, "layout-" + size.width + "x" + size.height);
            }
            edt(
                    () -> {
                        button(app, "sidebar.toggle").doClick();
                        assertFalse(((ToolSidebar) named(app, "sidebar")).isCompact());
                        button(app, "sidebar.toggle").doClick();
                    });
            // The collapsed rail still exposes category tools through keyboard-accessible popups.
            edt(() -> button(app, "nav.0").doClick());
            await(
                    () ->
                            javax.swing.MenuSelectionManager.defaultManager()
                                            .getSelectedPath()
                                            .length
                                    > 0);
            robot.keyPress(KeyEvent.VK_ESCAPE);
            robot.keyRelease(KeyEvent.VK_ESCAPE);
            edt(
                    () -> {
                        Component nav = named(app, "nav.0");
                        nav.requestFocusInWindow();
                    });
            robot.waitForIdle();
            robot.keyPress(KeyEvent.VK_TAB);
            robot.keyRelease(KeyEvent.VK_TAB);
            robot.waitForIdle();
            edt(
                    () ->
                            assertNotNull(
                                    KeyboardFocusManager.getCurrentKeyboardFocusManager()
                                            .getFocusOwner()));
            edt(() -> button(app, "view.fit").doClick());
            edt(() -> assertEquals("Tamanho real · 100%", button(app, "view.fit").getText()));
            // Escape closes a parameter dialog without changing the image.
            SwingUtilities.invokeLater(() -> app.getJMenuBar().getMenu(1).getItem(3).doClick());
            await(() -> parameterDialog() != null);
            robot.keyPress(KeyEvent.VK_ESCAPE);
            robot.keyRelease(KeyEvent.VK_ESCAPE);
            await(() -> parameterDialog() == null);
            edt(() -> assertTrue(status(app).contains("0 operações")));
        } finally {
            for (Window window : Window.getWindows())
                if (window instanceof JDialog) edt(window::dispose);
            edt(app::dispose);
        }
    }

    @Test
    void dropLoadsImageFromEmptyState() throws Exception {
        Painel app = open(1100, 720);
        Path path = dir.resolve("drop.png");
        ImageIO.write(Fixtures.shape("circle", Color.BLUE), "png", path.toFile());
        try {
            Transferable transferable =
                    new Transferable() {
                        public DataFlavor[] getTransferDataFlavors() {
                            return new DataFlavor[] {DataFlavor.javaFileListFlavor};
                        }

                        public boolean isDataFlavorSupported(DataFlavor f) {
                            return f.equals(DataFlavor.javaFileListFlavor);
                        }

                        public Object getTransferData(DataFlavor f)
                                throws UnsupportedFlavorException {
                            if (!isDataFlavorSupported(f)) throw new UnsupportedFlavorException(f);
                            return List.of(path.toFile());
                        }
                    };
            edt(
                    () -> {
                        JComponent empty = (JComponent) named(app, "workspace.empty");
                        assertTrue(
                                empty.getTransferHandler()
                                        .importData(
                                                new TransferHandler.TransferSupport(
                                                        empty, transferable)));
                    });
            await(() -> label(app, "workspace.document").getText().equals("drop.png"));
        } finally {
            edt(app::dispose);
        }
    }

    private static Painel open(int width, int height) throws Exception {
        AtomicReference<Painel> result = new AtomicReference<>();
        edt(
                () -> {
                    Painel app = new Painel();
                    app.setSize(width, height);
                    app.setLocation(20, 20);
                    app.setVisible(true);
                    result.set(app);
                });
        return result.get();
    }

    private static void edt(Runnable action) throws Exception {
        SwingUtilities.invokeAndWait(action);
    }

    private static void await(BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + 15_000_000_000L;
        while (System.nanoTime() < deadline) {
            AtomicReference<Boolean> ready = new AtomicReference<>(false);
            edt(() -> ready.set(condition.getAsBoolean()));
            if (ready.get()) return;
            Thread.sleep(20);
        }
        fail("A interface não concluiu a operação em 15 segundos.");
    }

    static Component named(Container root, String name) {
        if (name.equals(root.getName())) return root;
        for (Component c : root.getComponents()) {
            if (name.equals(c.getName())) return c;
            if (c instanceof Container child) {
                Component found = named(child, name);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static AbstractButton button(Container root, String name) {
        return (AbstractButton) named(root, name);
    }

    private static JLabel label(Container root, String name) {
        return (JLabel) named(root, name);
    }

    private static String status(Painel p) {
        return label(p, "workspace.status").getText();
    }

    private static JDialog parameterDialog() {
        for (Window w : Window.getWindows())
            if (w instanceof JDialog d && d.isShowing() && "dialog.parameters".equals(d.getName()))
                return d;
        return null;
    }

    private static void assertNoParameterDialog() throws Exception {
        edt(() -> assertNull(parameterDialog()));
    }

    private static void capture(Window window, String name) throws Exception {
        String directory = System.getProperty("pdi.screenshotDir");
        if (directory == null) return;
        Path folder = Path.of(directory);
        Files.createDirectories(folder);
        new Robot().waitForIdle();
        AtomicReference<Rectangle> bounds = new AtomicReference<>();
        edt(() -> bounds.set(window.getBounds()));
        ImageIO.write(
                new Robot().createScreenCapture(bounds.get()),
                "png",
                folder.resolve(name + ".png").toFile());
    }
}
