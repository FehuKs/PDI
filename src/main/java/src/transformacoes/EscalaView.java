package src.transformacoes;

import src.ui.Inputs;
import src.ui.ParameterForm;

import java.util.function.Consumer;

import javax.swing.JTextField;

public class EscalaView extends ParameterForm {
    public EscalaView(Consumer<Double> callback) {
        super("Altere o tamanho da imagem mantendo suas proporções.");
        JTextField value = new JTextField("1.0");
        value.setName("parameter.value");
        field("Fator de escala", value, "Ex.: 0,5 reduz pela metade; 2 dobra o tamanho.");
        submit(
                "Aplicar",
                () -> {
                    double factor;
                    try {
                        factor = Inputs.decimal(value.getText(), Double.MIN_NORMAL, 4000);
                    } catch (IllegalArgumentException ex) {
                        throw new IllegalArgumentException(
                                "Informe um fator maior que zero e de até 4000.", ex);
                    }
                    callback.accept(factor);
                });
    }
}
