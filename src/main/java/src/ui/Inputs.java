package src.ui;

import java.awt.Component;
import javax.swing.JOptionPane;

public final class Inputs {
    private Inputs() {}
    public static int integer(String value, int min, int max) {
        int parsed;
        try { parsed = Integer.parseInt(value.trim()); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("Digite um inteiro entre " + min + " e " + max + "."); }
        if (parsed < min || parsed > max) throw new IllegalArgumentException("Valor fora do intervalo " + min + " a " + max + ".");
        return parsed;
    }
    public static double decimal(String value, double min, double max) {
        double parsed;
        try { parsed = Double.parseDouble(value.trim().replace(',', '.')); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("Digite um número entre " + min + " e " + max + "."); }
        if (!Double.isFinite(parsed) || parsed < min || parsed > max) throw new IllegalArgumentException("Valor fora do intervalo " + min + " a " + max + ".");
        return parsed;
    }
    public static void apply(Component parent, Runnable work) {
        try { work.run(); }
        catch (IllegalArgumentException ex) { JOptionPane.showMessageDialog(parent, ex.getMessage(), "Entrada inválida", JOptionPane.WARNING_MESSAGE); }
    }
}
