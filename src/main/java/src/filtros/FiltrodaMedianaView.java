package src.filtros;

import src.ui.ActionForm;

public class FiltrodaMedianaView extends ActionForm {
    public FiltrodaMedianaView(Runnable callback) {
        super(
                "Reduza ruídos locais preservando as transições da imagem.",
                "Aplicar mediana",
                callback);
    }
}
