package src.desafio;

import javax.swing.*;
import java.awt.*;

public class AnalisadorBarrasView extends JPanel {

    public AnalisadorBarrasView(Runnable callback) {
        setLayout(new BorderLayout());
        JButton btn = new JButton("Aplicar Analisador Barras");
        btn.addActionListener(e -> callback.run());
        add(btn, BorderLayout.CENTER);
    }
}
    