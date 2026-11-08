package src.morfologia;

import javax.swing.*;
import java.awt.*;

public class Fechamentoview extends JPanel {

    public Fechamentoview(Runnable callback) {
        setLayout(new BorderLayout());
        JButton btn = new JButton("Aplicar Fechamento");
        btn.addActionListener(e -> callback.run());
        add(btn, BorderLayout.CENTER);
    }
}