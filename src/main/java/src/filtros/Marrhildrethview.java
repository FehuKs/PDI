package src.filtros;
import javax.swing.*;
import java.util.function.BiConsumer;

public class Marrhildrethview extends JPanel {
    public Marrhildrethview(BiConsumer<Double, Integer> callback) {
        JTextField sigma = new JTextField("1.4", 5), threshold = new JTextField("15", 5);
        JButton apply = new JButton("Aplicar");
        apply.addActionListener(e -> src.ui.Inputs.apply(this, () -> callback.accept(
            src.ui.Inputs.decimal(sigma.getText(), 0.1, 5), src.ui.Inputs.integer(threshold.getText(), 0, 2040))));
        add(new JLabel("Sigma (0,1 a 5)")); add(sigma); add(new JLabel("T (0 a 2040)")); add(threshold); add(apply);
    }
}
