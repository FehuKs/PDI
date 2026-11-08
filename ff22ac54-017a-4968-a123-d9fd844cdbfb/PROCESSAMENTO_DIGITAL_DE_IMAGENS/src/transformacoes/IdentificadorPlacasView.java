package src.desafio;

import javax.swing.*;
import java.awt.*;

public class IdentificadorPlacasView extends JPanel {

    public IdentificadorPlacasView(Runnable callback) {
        setLayout(new BorderLayout());
        JButton btn = new JButton("Aplicar Identificador Placas");
        btn.addActionListener(e -> callback.run());
        add(btn, BorderLayout.CENTER);
    }
}