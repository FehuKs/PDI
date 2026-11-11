package src.morfologia;

import src.ui.ActionForm;

public class Fechamentoview extends ActionForm {
    public Fechamentoview(Runnable callback) {
        super("Aplique dilatação seguida de erosão.", "Aplicar fechamento", callback);
    }
}
