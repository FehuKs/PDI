package src.filtros;

import javax.swing.*;
import java.awt.*;

public class FiltrodaMedianaView extends JPanel {

    public FiltrodaMedianaView(Runnable callback) {
        setLayout(new BorderLayout());
        JButton btn = new JButton("Aplicar Filtro de Mediana");
        btn.addActionListener(e -> callback.run());
        add(new JLabel("Clique para aplicar o Filtro da Mediana", SwingConstants.CENTER), BorderLayout.NORTH);
        add(btn, BorderLayout.CENTER);
    }
}