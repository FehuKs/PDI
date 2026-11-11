package src.morfologia;

import src.ui.ActionForm;

public class Erosaoview extends ActionForm {
    public Erosaoview(Runnable callback) {
        super("Reduza as regiões claras da imagem.", "Aplicar erosão", callback);
    }
}
