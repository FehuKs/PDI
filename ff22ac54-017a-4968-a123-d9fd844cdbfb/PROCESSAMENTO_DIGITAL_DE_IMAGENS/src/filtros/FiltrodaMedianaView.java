package src.filtros;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

public class FiltrodaMedianaView extends JPanel {
    private JButton applyButton;

    public FiltrodaMedianaView(Consumer<Void> applyCallback) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JLabel label = new JLabel("Clique para aplicar o Filtro da Mediana");

        applyButton = new JButton("Aplicar Filtro de Mediana");
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyCallback.accept(null); // Chama a função que aplica o filtro
            }
        });

        add(label);
        add(applyButton);
    }
}
