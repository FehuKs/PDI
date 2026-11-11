package src.transformacoes;

import src.ui.*;

import java.util.function.Consumer;

import javax.swing.JTextField;

public class TransladarView extends ParameterForm {
    public TransladarView(Consumer<int[]> callback) {
        super("Desloque a imagem na área atual. Partes fora dos limites serão cortadas.");
        JTextField x = new JTextField("0"), y = new JTextField("0");
        x.setName("parameter.x");
        y.setName("parameter.y");
        field("Horizontal", x, "Pixels. Positivo para a direita; negativo para a esquerda.");
        field("Vertical", y, "Pixels. Positivo para baixo; negativo para cima.");
        submit(
                "Transladar",
                () ->
                        callback.accept(
                                new int[] {
                                    Inputs.integer(x.getText(), -16000000, 16000000),
                                    Inputs.integer(y.getText(), -16000000, 16000000)
                                }));
    }
}
