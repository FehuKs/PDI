package src.filtros;

import javax.swing.*;
import java.awt.*;

public class BordasdeSobelView extends JPanel {

    public BordasdeSobelView(Runnable callback) {
        setLayout(new BorderLayout());
        JButton btn = new JButton("Aplicar Filtro de Bordas Sobel");
        btn.addActionListener(e -> callback.run());
        add(btn, BorderLayout.CENTER);
    }
}