package src.filtros;

import src.ui.ActionForm;

public class BordasdeSobelView extends ActionForm {
    public BordasdeSobelView(Runnable callback) {
        super(
                "Realce variações de intensidade para visualizar os contornos.",
                "Aplicar Sobel",
                callback);
    }
}
