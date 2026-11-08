package src.morfologia;

import javax.swing.*;
import java.awt.*;

public class Dilatacaoview extends JPanel {

    public Dilatacaoview(Runnable callback) {
        setLayout(new BorderLayout());
        JButton btn = new JButton("Aplicar Dilatacao");
        btn.addActionListener(e -> callback.run());
        add(btn, BorderLayout.CENTER);
    }
}