package src.desafio;

import src.ui.ActionForm;

public class AnalisadorBarrasView extends ActionForm {
    public AnalisadorBarrasView(Runnable callback) {
        super(
                "Compare barras verticais com rótulos ou escala legíveis.",
                "Analisar barras",
                callback);
    }
}
