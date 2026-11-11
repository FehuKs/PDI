package src.desafio;

import src.ui.ActionForm;

public class ContadorObjetosView extends ActionForm {
    public ContadorObjetosView(Runnable callback) {
        super(
                "Conte formas separadas nas cores vermelho, amarelo, verde e azul.",
                "Contar objetos",
                callback);
    }
}
