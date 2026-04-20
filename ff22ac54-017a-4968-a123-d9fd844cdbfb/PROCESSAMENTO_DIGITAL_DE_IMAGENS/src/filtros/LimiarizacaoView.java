package src.filtros;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.function.Consumer;

public class LimiarizacaoView extends JPanel {
    private JSlider slider;
    private JLabel labelValorLimiar;

    public LimiarizacaoView(Consumer<Integer> onApply) {
        setLayout(new BorderLayout());

        // Criação do JSlider para ajustar o limiar
        slider = new JSlider(JSlider.HORIZONTAL, 0, 255, 128); // Limiar inicial em 128
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        // Label que exibirá o valor atual do limiar
        labelValorLimiar = new JLabel("Valor atual: 128", JLabel.CENTER);
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // Atualiza o valor mostrado no JLabel conforme o slider é movido
                int valorLimiar = slider.getValue();
                labelValorLimiar.setText("Valor atual: " + valorLimiar);
            }
        });

        // Botão para aplicar o limiar selecionado
        JButton aplicarButton = new JButton("Aplicar Limiar");
        aplicarButton.addActionListener(e -> {
            int valorLimiar = slider.getValue();
            onApply.accept(valorLimiar);
        });

        // Adiciona o slider, o label e o botão ao painel
        add(slider, BorderLayout.CENTER);
        add(labelValorLimiar, BorderLayout.NORTH); // Label acima do slider
        add(aplicarButton, BorderLayout.SOUTH); // Botão abaixo do slider
    }
}

