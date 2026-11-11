package src.filtros;

import src.ui.Inputs;
import src.ui.ParameterForm;

import java.util.function.Consumer;

import javax.swing.JTextField;

public class BrilhoView extends ParameterForm {
    public BrilhoView(Consumer<Integer> callback) {
        super("Ajuste a luminosidade da imagem. O valor zero mantém o brilho atual.");
        JTextField value = new JTextField("0");
        value.setName("parameter.value");
        field("Intensidade", value, "De −255 a 255. Valores negativos escurecem a imagem.");
        submit("Aplicar", () -> callback.accept(Inputs.integer(value.getText(), -255, 255)));
    }
}
