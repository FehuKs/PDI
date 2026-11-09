package src.transformacoes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class EspelhamentoView extends JPanel {

    private boolean horizontal;
    private boolean vertical;

    public EspelhamentoView(Consumer<boolean[]> callback) {
        super();
        setBorder(BorderFactory.createTitledBorder("Espelhamento"));

        JCheckBox chkHorizontal = new JCheckBox("Horizontal");
        JCheckBox chkVertical = new JCheckBox("Vertical");

        chkHorizontal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                horizontal = chkHorizontal.isSelected();
            }
        });

        chkVertical.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                vertical = chkVertical.isSelected();
            }
        });

        JButton aplicar = new JButton("Aplicar");
        aplicar.addActionListener((e) -> {
            boolean[] params = { horizontal, vertical };
            callback.accept(params);
        });

        add(new JLabel("Escolha o tipo de espelhamento:"));
        add(chkHorizontal);
        add(chkVertical);
        add(aplicar);
    }
}
