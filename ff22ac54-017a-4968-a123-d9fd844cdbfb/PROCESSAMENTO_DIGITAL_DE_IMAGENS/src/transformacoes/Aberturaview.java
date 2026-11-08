package src.morfologia;


import javax.swing.*;
import java.awt.*;

public class Aberturaview extends JPanel {

    public Aberturaview(Runnable callback) {
        setLayout(new BorderLayout());
        JButton btn = new JButton("Aplicar Abertura");
        btn.addActionListener(e -> callback.run());
        add(btn, BorderLayout.CENTER);
    }
}