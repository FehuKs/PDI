package src.desafio;

import javax.swing.*;
import java.awt.*;

public class LeitorRelogioView extends JPanel {

    public LeitorRelogioView(Runnable callback) {
        setLayout(new BorderLayout());
        JButton btn = new JButton("Aplicar Leitor Relogio");
        btn.addActionListener(e -> callback.run());
        add(btn, BorderLayout.CENTER);
    }
}
    