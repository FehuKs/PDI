package src.filtros;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ContrasteView extends JPanel {

    private JSlider sliderContraste;
    private JLabel valorContrasteLabel;

    public ContrasteView(Consumer<Float> applyFilterCallback) {
        setLayout(new BorderLayout());

        // Cria o slider para ajustar o fator de contraste
        sliderContraste = new JSlider(0, 200, 100); // Valores de 0 a 200, sendo 100 o valor neutro
        sliderContraste.setMajorTickSpacing(50);
        sliderContraste.setPaintTicks(true);
        sliderContraste.setPaintLabels(true);

        valorContrasteLabel = new JLabel("Fator de Contraste: 1.0");
        sliderContraste.addChangeListener(e -> {
            float valorContraste = sliderContraste.getValue() / 100.0f;
            valorContrasteLabel.setText(String.format("Fator de Contraste: %.2f", valorContraste));
        });

        // Botão para aplicar o filtro de contraste
        JButton applyButton = new JButton("Aplicar Filtro de Contraste");
        applyButton.addActionListener(e -> {
            float valorContraste = sliderContraste.getValue() / 100.0f;
            applyFilterCallback.accept(valorContraste);
        });

        // Adiciona os componentes ao painel
        add(valorContrasteLabel, BorderLayout.NORTH);
        add(sliderContraste, BorderLayout.CENTER);
        add(applyButton, BorderLayout.SOUTH);
    }
}
