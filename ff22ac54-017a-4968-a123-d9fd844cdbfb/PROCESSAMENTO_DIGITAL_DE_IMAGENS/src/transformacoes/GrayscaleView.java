package src.filtros;

import javax.swing.*;
import java.awt.*;

public class GrayscaleView extends JPanel {

    public GrayscaleView(Runnable callback) {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("Aplicar Filtro Grayscale", SwingConstants.CENTER);
        add(label, BorderLayout.NORTH);
        JButton btn = new JButton("Aplicar");
        btn.addActionListener(e -> callback.run());
        add(btn, BorderLayout.SOUTH);
    }
}