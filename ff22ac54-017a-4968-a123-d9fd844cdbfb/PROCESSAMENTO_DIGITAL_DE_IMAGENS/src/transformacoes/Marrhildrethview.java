package src.filtros;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.function.BiConsumer;
import javax.swing.*;

/**
 * Painel de parâmetros para o operador de Marr-Hildreth.
 * Expõe os dois parâmetros do algoritmo:
 *   σ (sigma)  — desvio padrão da Gaussiana
 *   T (limiar) — limiar para o zero crossing
 */
public class Marrhildrethview extends JPanel {

    private double sigma  = 1.4; // valor padrão razoável para a maioria das imagens
    private int    limiar = 15;

    /**
     * @param callback Recebe (sigma, limiar) quando o usuário clica em Aplicar
     */
    public Marrhildrethview(BiConsumer<Double, Integer> callback) {
        super();
        setBorder(BorderFactory.createTitledBorder("Marr-Hildreth (LoG)"));

        // --- Campo σ ---
        JLabel lblSigma = new JLabel("σ — desvio padrão Gaussiano (ex.: 1.0 a 3.0):");
        JTextField txtSigma = new JTextField(String.valueOf(sigma), 5);
        txtSigma.addFocusListener(new FocusListener() {
            @Override public void focusGained(FocusEvent e) {}
            @Override
            public void focusLost(FocusEvent e) {
                try {
                    double v = Double.parseDouble(txtSigma.getText());
                    if (v <= 0) throw new NumberFormatException();
                    sigma = v;
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(Marrhildrethview.this,
                            "σ deve ser um número positivo (ex.: 1.4).",
                            "Entrada inválida", JOptionPane.WARNING_MESSAGE);
                    txtSigma.setText(String.valueOf(sigma));
                }
            }
        });

        // --- Campo T ---
        JLabel lblLimiar = new JLabel("T — limiar zero crossing (ex.: 5 a 30):");
        JTextField txtLimiar = new JTextField(String.valueOf(limiar), 5);
        txtLimiar.addFocusListener(new FocusListener() {
            @Override public void focusGained(FocusEvent e) {}
            @Override
            public void focusLost(FocusEvent e) {
                try {
                    int v = Integer.parseInt(txtLimiar.getText());
                    if (v < 0) throw new NumberFormatException();
                    limiar = v;
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(Marrhildrethview.this,
                            "T deve ser um inteiro não negativo (ex.: 15).",
                            "Entrada inválida", JOptionPane.WARNING_MESSAGE);
                    txtLimiar.setText(String.valueOf(limiar));
                }
            }
        });

        JButton aplicar = new JButton("Aplicar");
        aplicar.addActionListener(e -> callback.accept(sigma, limiar));

        add(lblSigma);
        add(txtSigma);
        add(lblLimiar);
        add(txtLimiar);
        add(aplicar);
    }
}