package src.ui;

import java.awt.*;
import java.awt.event.KeyEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public final class Dialogs {
    private Dialogs() {}

    public static JDialog parameters(Window owner, String title, JPanel content) {
        return create(owner, title, content, true);
    }

    private static JDialog create(Window owner, String title, JPanel content, boolean cancellable) {
        JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.DOCUMENT_MODAL);
        dialog.setName("dialog.parameters");
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel root =
                new JPanel(new BorderLayout(0, Theme.XL)) {
                    @Override
                    public Dimension getPreferredSize() {
                        Dimension d = super.getPreferredSize();
                        return new Dimension(Theme.DIALOG_WIDTH, d.height);
                    }
                };
        root.setBackground(Theme.PAPER);
        root.setBorder(new EmptyBorder(Theme.XL, Theme.XL, Theme.XL, Theme.XL));
        JPanel heading = Theme.panel(new BorderLayout(Theme.MD, 0), Theme.PAPER);
        heading.add(Theme.label(title, Theme.TITLE, Theme.TEXT), BorderLayout.CENTER);
        root.add(heading, BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);
        dialog.setContentPane(root);
        dialog.getRootPane()
                .registerKeyboardAction(
                        e -> dialog.dispose(),
                        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                        JComponent.WHEN_IN_FOCUSED_WINDOW);
        if (content instanceof ParameterForm form) {
            if (cancellable) form.cancel(dialog::dispose);
            dialog.getRootPane().setDefaultButton(form.submitButton());
        }
        fitToScreen(dialog);
        dialog.setResizable(false);
        return dialog;
    }

    public static void fitToScreen(JDialog dialog) {
        dialog.pack();
        GraphicsConfiguration configuration = dialog.getGraphicsConfiguration();
        Rectangle screen = configuration.getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(configuration);
        int width = screen.width - insets.left - insets.right - 2 * Theme.XL;
        int height = screen.height - insets.top - insets.bottom - 2 * Theme.XL;
        dialog.setSize(Math.min(dialog.getWidth(), width), Math.min(dialog.getHeight(), height));
        dialog.setLocationRelativeTo(dialog.getOwner());
        dialog.setLocation(
                Math.max(
                        screen.x + insets.left,
                        Math.min(
                                dialog.getX(),
                                screen.x + screen.width - insets.right - dialog.getWidth())),
                Math.max(
                        screen.y + insets.top,
                        Math.min(
                                dialog.getY(),
                                screen.y + screen.height - insets.bottom - dialog.getHeight())));
        // setSize may cap the packed height: lay out the scrollable body and fixed actions again.
        dialog.validate();
    }

    public static void message(Window owner, String title, String text) {
        ParameterForm content = new ParameterForm(text);
        content.submit("Fechar", () -> SwingUtilities.getWindowAncestor(content).dispose());
        create(owner, title, content, false).setVisible(true);
    }

    public static boolean confirm(Window owner, String title, String text, String action) {
        boolean[] accepted = {false};
        ParameterForm content = new ParameterForm(text);
        content.submit(
                action,
                AppButton.Style.SECONDARY,
                () -> {
                    accepted[0] = true;
                    SwingUtilities.getWindowAncestor(content).dispose();
                });
        parameters(owner, title, content).setVisible(true);
        return accepted[0];
    }
}
