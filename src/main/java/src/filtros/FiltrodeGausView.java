package src.filtros;

import src.ui.ActionForm;

public class FiltrodeGausView extends ActionForm {
    public FiltrodeGausView(Runnable callback) {
        super("Suavize a imagem com um filtro gaussiano.", "Aplicar Gaussiano", callback);
    }
}
