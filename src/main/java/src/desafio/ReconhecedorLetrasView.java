package src.desafio;
 
import javax.swing.*;
import java.awt.*;

public class ReconhecedorLetrasView extends JPanel {

    public ReconhecedorLetrasView(Runnable callback) {
        setLayout(new BorderLayout());
        JButton btn = new JButton("Aplicar Reconhecedor Letras");
        btn.addActionListener(e -> callback.run());
        add(btn, BorderLayout.CENTER);
    }
}


