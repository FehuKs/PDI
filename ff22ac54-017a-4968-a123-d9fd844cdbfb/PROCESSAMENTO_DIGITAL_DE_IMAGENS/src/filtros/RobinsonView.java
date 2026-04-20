package src.filtros;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class RobinsonView extends JPanel {

    public RobinsonView(Consumer<Void> applyFilterCallback) {
        setLayout(new BorderLayout());

        // Adiciona botão para aplicar o filtro de Robinson
        JButton applyButton = new JButton("Aplicar Filtro Robinson");
        applyButton.addActionListener(e -> applyFilterCallback.accept(null));

        add(applyButton, BorderLayout.CENTER);
    }
}
