package src.desafio;

import src.ui.ActionForm;

public class ReconhecedorLetrasView extends ActionForm {
    public ReconhecedorLetrasView(Runnable callback) {
        super(
                "Identifique letras maiúsculas escuras sobre fundo claro.",
                "Reconhecer letras",
                callback);
    }
}
