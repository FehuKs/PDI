package src.filtros;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class FiltrodeGausView extends JPanel {

    public FiltrodeGausView(Consumer<Void> applyFilterCallback) {
        setLayout(new BorderLayout());

        // Adiciona botão para aplicar o filtro
        JButton applyButton = new JButton("Aplicar Filtro Gaussiano");
        applyButton.addActionListener(e -> applyFilterCallback.accept(null));

        add(applyButton, BorderLayout.CENTER);
    }
}
