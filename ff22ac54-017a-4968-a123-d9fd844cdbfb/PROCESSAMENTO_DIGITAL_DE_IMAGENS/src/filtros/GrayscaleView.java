package src.filtros;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GrayscaleView extends JPanel {

    public GrayscaleView(ActionListener listener) {
        setLayout(new BorderLayout());

        JLabel label = new JLabel("Aplicar Filtro Grayscale");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label, BorderLayout.NORTH);

        JButton applyButton = new JButton("Aplicar");
        applyButton.addActionListener(listener);
        add(applyButton, BorderLayout.SOUTH);
    }
}

