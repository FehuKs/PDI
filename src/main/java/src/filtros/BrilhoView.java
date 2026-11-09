package src.filtros;
import javax.swing.*;
import java.util.function.Consumer;

public class BrilhoView extends JPanel {
    public BrilhoView(Consumer<Integer> callback) {
        setBorder(BorderFactory.createTitledBorder("Brilho"));
        JTextField value = new JTextField("0", 7);
        JButton apply = new JButton("Aplicar");
        apply.addActionListener(e -> src.ui.Inputs.apply(this, () -> callback.accept(src.ui.Inputs.integer(value.getText(), -255, 255))));
        add(new JLabel("Brilho (-255 a 255)")); add(value); add(apply);
    }
}
