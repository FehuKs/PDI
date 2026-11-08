package src.transformacoes;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class TransladarView extends JPanel {

    /** Parâmetros para translação da imagem */
    private int deslocamentoX;
    private int deslocamentoY;

    
    public TransladarView(Consumer<int[]> callback) {
        super();
        this.deslocamentoX = 0;
        this.deslocamentoY = 0;

        // Configuração do layout do painel
        setBorder(BorderFactory.createTitledBorder("Translação"));

        // Campo de texto para inserir o deslocamento horizontal
        JTextField deslocamentoXField = new JTextField(5);
        deslocamentoXField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {}

            @Override
            public void focusLost(FocusEvent e) {
                try {
                    // Atualiza o parâmetro de deslocamento horizontal ao perder o foco
                    deslocamentoX = Integer.parseInt(deslocamentoXField.getText());
                } catch (NumberFormatException ex) {
                    deslocamentoX = 0; // Deslocamento padrão caso haja erro de entrada
                }
            }
        });

        // Campo de texto para inserir o deslocamento vertical
        JTextField deslocamentoYField = new JTextField(5);
        deslocamentoYField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {}

            @Override
            public void focusLost(FocusEvent e) {
                try {
                    // Atualiza o parâmetro de deslocamento vertical ao perder o foco
                    deslocamentoY = Integer.parseInt(deslocamentoYField.getText());
                } catch (NumberFormatException ex) {
                    deslocamentoY = 0; // Deslocamento padrão caso haja erro de entrada
                }
            }
        });

        // Botão para aplicar a translação
        JButton aplicar = new JButton("Aplicar");
        aplicar.addActionListener((e) -> callback.accept(new int[]{deslocamentoX, deslocamentoY}));

        // Adiciona os componentes ao painel
        add(new JLabel("Deslocamento X"));
        add(deslocamentoXField);
        add(new JLabel("Deslocamento Y"));
        add(deslocamentoYField);
        add(aplicar);
    }
}
