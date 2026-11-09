package src.transformacoes;
import javax.swing.*;
import java.util.function.Consumer;

public class RotacionarView extends JPanel {
    public RotacionarView(Consumer<int[]> callback) {
        JTextField angle = new JTextField("0", 5);
        JComboBox<String> direction = new JComboBox<>(new String[]{"Direita", "Esquerda"});
        JButton apply = new JButton("Aplicar");
        apply.addActionListener(e -> src.ui.Inputs.apply(this, () -> callback.accept(new int[]{
            src.ui.Inputs.integer(angle.getText(), -360, 360), direction.getSelectedIndex() == 0 ? 1 : 0})));
        add(new JLabel("Ângulo (-360 a 360)")); add(angle); add(direction); add(apply);
    }
}
