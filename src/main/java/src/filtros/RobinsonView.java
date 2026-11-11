package src.filtros;

import src.ui.ActionForm;

public class RobinsonView extends ActionForm {
    public RobinsonView(Runnable callback) {
        super("Visualize contornos em diferentes direções.", "Aplicar Robinson", callback);
    }
}
