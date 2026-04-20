package src.filtros;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class BordasdeSobelView extends JPanel {

    public BordasdeSobelView(Consumer<Void> applyFilterCallback) {
        setLayout(new BorderLayout());

        // Adiciona botão para aplicar o filtro
        JButton applyButton = new JButton("Aplicar Filtro de Bordas Sobel");
        applyButton.addActionListener(e -> applyFilterCallback.accept(null));

        add(applyButton, BorderLayout.CENTER);
    }
}
