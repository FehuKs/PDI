package src.filtros;

import src.ui.*;

import java.util.function.BiConsumer;

import javax.swing.JTextField;

public class Marrhildrethview extends ParameterForm {
    public Marrhildrethview(BiConsumer<Double, Integer> callback) {
        super("Detecte contornos com suavização prévia. Ajustes maiores podem levar mais tempo.");
        JTextField sigma = new JTextField("1.4"), threshold = new JTextField("15");
        field("Suavização (sigma)", sigma, "De 0,1 a 5. Um valor maior suaviza mais detalhes.");
        field(
                "Limiar dos contornos",
                threshold,
                "De 0 a 2040. Aumente para descartar mudanças mais fracas.");
        submit(
                "Detectar contornos",
                () ->
                        callback.accept(
                                Inputs.decimal(sigma.getText(), 0.1, 5),
                                Inputs.integer(threshold.getText(), 0, 2040)));
    }
}
