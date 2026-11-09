package src.transformacoes;
import javax.swing.*;
import java.util.function.Consumer;

public class TransladarView extends JPanel {
    public TransladarView(Consumer<int[]> callback) {
        JTextField x = new JTextField("0", 6), y = new JTextField("0", 6);
        JButton apply = new JButton("Aplicar");
        apply.addActionListener(e -> src.ui.Inputs.apply(this, () -> callback.accept(new int[]{
            src.ui.Inputs.integer(x.getText(), -16000000, 16000000), src.ui.Inputs.integer(y.getText(), -16000000, 16000000)})));
        add(new JLabel("Deslocamento X")); add(x); add(new JLabel("Deslocamento Y")); add(y); add(apply);
    }
}
