package src.desafio;
  

import javax.swing.*;
import java.awt.*;

public class ContadorObjetosView extends JPanel {

    public ContadorObjetosView(Runnable callback) {
        setLayout(new BorderLayout());
        JButton btn = new JButton("Aplicar Contador Objetos");
        btn.addActionListener(e -> callback.run());
        add(btn, BorderLayout.CENTER);
    }
}
    

