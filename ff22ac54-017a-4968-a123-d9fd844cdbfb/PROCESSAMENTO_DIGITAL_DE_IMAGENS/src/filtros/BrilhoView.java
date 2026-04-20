package src.filtros;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class BrilhoView extends JPanel {

    private int brilho;

    public BrilhoView(Consumer<Integer> callback) {
        super();
        setBorder(BorderFactory.createTitledBorder("Ajuste de Brilho"));

        JLabel lblBrilho = new JLabel("Valor de Brilho (Ex.: 50 para aumentar, -50 para diminuir):");
        JTextField txtBrilho = new JTextField(5);

        txtBrilho.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {}

            @Override
            public void focusLost(FocusEvent e) {
                brilho = Integer.parseInt(txtBrilho.getText());
            }
        });

        JButton aplicar = new JButton("Aplicar");
        aplicar.addActionListener((ActionEvent e) -> {
            callback.accept(brilho);
        });

        add(lblBrilho);
        add(txtBrilho);
        add(aplicar);
    }
}
