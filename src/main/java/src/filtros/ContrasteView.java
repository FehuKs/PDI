package src.filtros;

import src.ui.ParameterForm;

import java.util.function.Consumer;

import javax.swing.*;

public class ContrasteView extends ParameterForm {
    public ContrasteView(Consumer<Float> callback) {
        super("Ajuste a diferença entre áreas claras e escuras. O valor 1 mantém o contraste.");
        JSlider slider = new JSlider(0, 200, 100);
        slider.setName("parameter.slider");
        slider.setMajorTickSpacing(50);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        JLabel value = new JLabel();
        Runnable update = () -> value.setText(String.valueOf(slider.getValue() / 100.0f));
        update.run();
        slider.addChangeListener(e -> update.run());
        field("Fator de contraste", slider, null);
        field("Valor atual", value, null);
        submit("Aplicar", () -> callback.accept(slider.getValue() / 100.0f));
    }
}
