package src.filtros;

import src.ui.ParameterForm;

import java.util.function.Consumer;

import javax.swing.*;

public class LimiarizacaoView extends ParameterForm {
    public LimiarizacaoView(Consumer<Integer> callback) {
        super("Separe a imagem em áreas claras e escuras a partir de um limiar.");
        JSlider slider = new JSlider(0, 255, 128);
        slider.setName("parameter.slider");
        slider.setMajorTickSpacing(50);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        JLabel value = new JLabel();
        Runnable update = () -> value.setText(String.valueOf(slider.getValue()));
        update.run();
        slider.addChangeListener(e -> update.run());
        field("Limiar", slider, null);
        field("Valor atual", value, null);
        submit("Aplicar", () -> callback.accept(slider.getValue()));
    }
}
