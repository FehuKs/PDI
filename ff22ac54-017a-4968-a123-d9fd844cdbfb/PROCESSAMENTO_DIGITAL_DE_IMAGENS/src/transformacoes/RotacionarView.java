package src.transformacoes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class RotacionarView extends JPanel {

   
    private int angle;
    private boolean isRight;
    
    
    public RotacionarView(Consumer<int[]> callback) {
        super();
        this.angle = 0;
        this.isRight = true; // Direção padrão: direita
        
        // Configuração do layout do painel
        setBorder(BorderFactory.createTitledBorder("Rotação"));
        
        // ComboBox para selecionar a direção da rotação
        JComboBox<String> rotationType = new JComboBox<>(new String[] {"Direita", "Esquerda"});
        rotationType.setSelectedIndex(0); // Padrão: direita
        rotationType.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Ajusta os parâmetros com base na escolha
                isRight = rotationType.getSelectedIndex() == 0;
            }
        });
        
        // Campo de texto para inserir o ângulo de rotação
        JTextField rotationDegrees = new JTextField(3);
        rotationDegrees.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {}

            @Override
            public void focusLost(FocusEvent e) {
                try {
                    // Atualiza o parâmetro de ângulo ao perder o foco
                    angle = Integer.parseInt(rotationDegrees.getText());
                } catch (NumberFormatException ex) {
                    angle = 0; // Ângulo padrão caso haja erro de entrada
                }
            }
        });
        
        // Botão para aplicar a rotação
        JButton aplicar = new JButton("Aplicar");
        aplicar.addActionListener((e) -> callback.accept(new int[]{angle, isRight ? 1 : 0}));
        
        // Adiciona os componentes ao painel
        add(new JLabel("Direção"));
        add(rotationType);
        add(new JLabel("Ângulo"));
        add(rotationDegrees);
        add(aplicar);
    }
}
