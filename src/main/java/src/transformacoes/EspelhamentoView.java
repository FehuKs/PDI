package src.transformacoes;

import src.ui.ParameterForm;

import java.util.function.Consumer;

import javax.swing.*;

public class EspelhamentoView extends ParameterForm {
    public EspelhamentoView(Consumer<boolean[]> callback) {
        super("Inverta a imagem em um eixo ou nos dois ao mesmo tempo.");
        JCheckBox horizontal = new JCheckBox("Espelhar na horizontal");
        JCheckBox vertical = new JCheckBox("Espelhar na vertical");
        horizontal.setMnemonic('H');
        vertical.setMnemonic('V');
        field("Eixos", horizontal, null);
        field("", vertical, null);
        submit(
                "Espelhar",
                () ->
                        callback.accept(
                                new boolean[] {horizontal.isSelected(), vertical.isSelected()}));
    }
}
