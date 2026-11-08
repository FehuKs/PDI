package src.filtros;

import javax.swing.*;
import java.awt.*;

public class RobinsonView extends JPanel {

    public RobinsonView(Runnable callback) {
        setLayout(new BorderLayout());
        JButton btn = new JButton("Aplicar Filtro Robinson");
        btn.addActionListener(e -> callback.run());
        add(btn, BorderLayout.CENTER);
    }
}