package src.transformacoes;
import javax.swing.*;
import java.util.function.Consumer;

public class EscalaView extends JPanel {
    public EscalaView(Consumer<Double> callback) {
        setBorder(BorderFactory.createTitledBorder("Escala"));
        JTextField value = new JTextField("1.0", 7);
        JButton apply = new JButton("Aplicar");
        apply.addActionListener(e -> src.ui.Inputs.apply(this, () -> callback.accept(src.ui.Inputs.decimal(value.getText(), Double.MIN_NORMAL, 4000))));
        add(new JLabel("Fator de escala (ex.: 0,5 ou 1,5)")); add(value); add(apply);
    }
}
