package src.transformacoes;

import src.ui.*;

import java.util.function.Consumer;

import javax.swing.*;

public class RotacionarView extends ParameterForm {
    public RotacionarView(Consumer<int[]> callback) {
        super("Gire a imagem. A área de resultado se adapta à rotação.");
        JTextField angle = new JTextField("0");
        angle.setName("parameter.angle");
        JComboBox<String> direction = new JComboBox<>(new String[] {"Direita", "Esquerda"});
        field("Ângulo", angle, "De −360° a 360°.");
        field("Direção", direction, null);
        submit(
                "Rotacionar",
                () ->
                        callback.accept(
                                new int[] {
                                    Inputs.integer(angle.getText(), -360, 360),
                                    direction.getSelectedIndex() == 0 ? 1 : 0
                                }));
    }
}
