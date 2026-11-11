package src.desafio;

import src.ui.ActionForm;

public class IdentificadorPlacasView extends ActionForm {
    public IdentificadorPlacasView(Runnable callback) {
        super(
                "Analise candidatos a placas de trânsito em imagens simples.",
                "Identificar placas",
                callback);
    }
}
