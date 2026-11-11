package src.ui;

/** Compatibility panel for parameter-free operations used outside the direct workspace actions. */
public class ActionForm extends ParameterForm {
    public ActionForm(String description, String action, Runnable callback) {
        super(description);
        submit(action, callback);
    }
}
