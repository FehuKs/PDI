package src.filtros;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.function.Consumer;
import javax.swing.*;

public class BrilhoView extends JPanel {

    private int brilho = 0;

    public BrilhoView(Consumer<Integer> callback) {
        super();
        setBorder(BorderFactory.createTitledBorder("Ajuste de Brilho"));

        JLabel lblBrilho = new JLabel("Valor (ex.: 50 para aumentar, -50 para diminuir):");
        JTextField txtBrilho = new JTextField(5);

        txtBrilho.addFocusListener(new FocusListener() {
            @Override public void focusGained(FocusEvent e) {}

            @Override
            public void focusLost(FocusEvent e) {
                try {
                    brilho = Integer.parseInt(txtBrilho.getText());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(BrilhoView.this,
                            "Digite um número inteiro válido (ex.: 50 ou -30).",
                            "Entrada inválida", JOptionPane.WARNING_MESSAGE);
                    txtBrilho.setText(String.valueOf(brilho));
                }
            }
        });

        JButton aplicar = new JButton("Aplicar");
        aplicar.addActionListener(e -> callback.accept(brilho));

        add(lblBrilho);
        add(txtBrilho);
        add(aplicar);
    }
}