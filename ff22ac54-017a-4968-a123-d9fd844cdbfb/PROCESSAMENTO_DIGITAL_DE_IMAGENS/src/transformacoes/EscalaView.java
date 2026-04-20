package src.transformacoes;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class EscalaView extends JPanel {

    private double scaleFactor = 1.0; // valor padrão seguro

    public EscalaView(Consumer<Double> callback) {
        super();
        setBorder(BorderFactory.createTitledBorder("Escala"));

        JLabel lblScale = new JLabel("Fator de Escala (Ex.: 1.5 para aumentar, 0.5 para diminuir):");
        JTextField txtScaleFactor = new JTextField(5);

        txtScaleFactor.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {}

            @Override
            public void focusLost(FocusEvent e) {
                try {
                    double valor = Double.parseDouble(txtScaleFactor.getText());
                    if (valor <= 0) {
                        JOptionPane.showMessageDialog(EscalaView.this,
                            "O fator de escala deve ser maior que zero.",
                            "Valor inválido", JOptionPane.WARNING_MESSAGE);
                        txtScaleFactor.setText(String.valueOf(scaleFactor));
                    } else {
                        scaleFactor = valor;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(EscalaView.this,
                        "Digite um número válido para o fator de escala (ex.: 1.5).",
                        "Entrada inválida", JOptionPane.WARNING_MESSAGE);
                    txtScaleFactor.setText(String.valueOf(scaleFactor));
                }
            }
        });

        JButton aplicar = new JButton("Aplicar");
        aplicar.addActionListener((e) -> callback.accept(scaleFactor));

        add(lblScale);
        add(txtScaleFactor);
        add(aplicar);
    }
}