package src.afinamento;

import javax.swing.*;
import java.awt.*;

public class ZhangSuenView extends JPanel {

    public ZhangSuenView(Runnable callback) {
        setLayout(new BorderLayout());
        JButton btn = new JButton("Aplicar Thinning Zhang-Suen");
        btn.addActionListener(e -> callback.run());
        add(btn, BorderLayout.CENTER);
    }
}