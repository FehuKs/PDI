package src.morfologia;

import src.ui.ActionForm;

public class Aberturaview extends ActionForm {
    public Aberturaview(Runnable callback) {
        super("Aplique erosão seguida de dilatação.", "Aplicar abertura", callback);
    }
}
