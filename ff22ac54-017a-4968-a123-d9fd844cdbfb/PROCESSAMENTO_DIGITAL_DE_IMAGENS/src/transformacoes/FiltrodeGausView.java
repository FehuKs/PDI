package src.filtros;

import javax.swing.*;
import java.awt.*;

public class FiltrodeGausView extends JPanel {

    public FiltrodeGausView(Runnable callback) {
        setLayout(new BorderLayout());
        JButton btn = new JButton("Aplicar Filtro Gaussiano");
        btn.addActionListener(e -> callback.run());
        add(btn, BorderLayout.CENTER);
    }
}