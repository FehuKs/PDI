package src.morfologia;

import javax.swing.*;
import java.awt.*;

public class Erosaoview extends JPanel {

    public Erosaoview(Runnable callback) {
        setLayout(new BorderLayout());
        JButton btn = new JButton("Aplicar Erosao");
        btn.addActionListener(e -> callback.run());
        add(btn, BorderLayout.CENTER);
    }
}