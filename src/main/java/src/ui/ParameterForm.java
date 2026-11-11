package src.ui;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Labelled controls, one submit action and inline validation shared by all parameter panels. */
public class ParameterForm extends JPanel {
    private final JPanel fields = Theme.panel(new GridBagLayout(), Theme.PAPER);
    private final JTextArea error = Theme.text("");
    private final JPanel footer = Theme.panel(new BorderLayout(), Theme.PAPER);
    private JButton submit;
    private int row;

    public ParameterForm(String description) {
        super(new BorderLayout(0, Theme.XL));
        setBackground(Theme.PAPER);
        JTextArea help = Theme.text(description);
        help.setForeground(Theme.MUTED);
        help.setFont(Theme.BODY);
        help.setRows(2);
        help.setFocusable(false);
        JPanel body = Theme.panel(new BorderLayout(0, Theme.LG), Theme.PAPER);
        body.add(help, BorderLayout.NORTH);
        body.add(fields, BorderLayout.CENTER);
        JScrollPane scroll = Theme.scroll(body);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
        error.setName("form.error");
        error.getAccessibleContext().setAccessibleName("Erro de validação");
        error.setFont(Theme.SMALL);
        error.setRows(2);
        error.setBorder(new EmptyBorder(0, 0, Theme.MD, 0));
        error.setVisible(false);
        footer.add(error, BorderLayout.NORTH);
        add(footer, BorderLayout.SOUTH);
    }

    public void field(String text, JComponent input, String hint) {
        JLabel label = Theme.label(text, Theme.SECTION, Theme.TEXT);
        label.setLabelFor(input);
        if (!text.isBlank() && !(input instanceof JLabel))
            input.getAccessibleContext().setAccessibleName(text);
        if (input instanceof JTextField || input instanceof JComboBox<?>) Theme.field(input);
        if (input instanceof JSlider slider) Theme.slider(slider);
        if (hint != null) input.getAccessibleContext().setAccessibleDescription(hint);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row++;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, Theme.SM, 0);
        if (!text.isBlank()) {
            fields.add(label, c);
            c.gridy = row++;
        }
        fields.add(input, c);
        if (hint != null && !hint.isBlank()) {
            c.gridy = row++;
            c.insets = new Insets(0, 0, Theme.XL, 0);
            fields.add(Theme.label(hint, Theme.SMALL, Theme.MUTED), c);
        } else {
            c.gridy = row++;
            fields.add(Box.createVerticalStrut(Theme.MD), c);
        }
    }

    public void submit(String text, Runnable callback) {
        submit(text, AppButton.Style.PRIMARY, callback);
    }

    public void submit(String text, AppButton.Style style, Runnable callback) {
        submit = new AppButton(text, null, style);
        submit.setName("form.submit");
        submit.addActionListener(
                e -> {
                    error.setVisible(false);
                    try {
                        callback.run();
                    } catch (IllegalArgumentException ex) {
                        showError(ex.getMessage());
                    }
                });
        footer.add(submit, BorderLayout.EAST);
    }

    public void cancel(Runnable callback) {
        AppButton cancel = new AppButton("Cancelar", null, AppButton.Style.GHOST);
        cancel.setName("form.cancel");
        cancel.setToolTipText("Fechar sem aplicar · Esc");
        cancel.addActionListener(e -> callback.run());
        footer.add(cancel, BorderLayout.WEST);
    }

    public JButton submitButton() {
        return submit;
    }

    public void showError(String message) {
        error.setText(message);
        error.setVisible(true);
        error.getAccessibleContext().setAccessibleDescription(message);
        firePropertyChange("validationError", null, message);
        revalidate();
        repaint();
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (owner instanceof JDialog dialog) Dialogs.fitToScreen(dialog);
    }
}
