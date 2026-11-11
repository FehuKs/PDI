package src;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import src.filtros.*;
import src.transformacoes.*;
import src.ui.*;

import java.awt.*;
import java.util.concurrent.atomic.*;
import java.util.function.Consumer;

import javax.swing.*;

class FormsTest {
    @BeforeAll
    static void theme() throws Exception {
        SwingUtilities.invokeAndWait(Theme::install);
    }

    @Test
    void scaleAndBrightnessRejectInvalidInputInline() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    AtomicReference<Double> scale = new AtomicReference<>();
                    EscalaView form = new EscalaView(scale::set);
                    JTextField field = find(form, JTextField.class);
                    field.setText("NaN");
                    form.submitButton().doClick();
                    assertNull(scale.get());
                    assertTrue(findError(form).isVisible());
                    field.setText("1,5");
                    form.submitButton().doClick();
                    assertEquals(1.5, scale.get());
                    assertFalse(findError(form).isVisible());
                    AtomicReference<Integer> brightness = new AtomicReference<>();
                    BrilhoView light = new BrilhoView(brightness::set);
                    JTextField value = find(light, JTextField.class);
                    value.setText("256");
                    light.submitButton().doClick();
                    assertNull(brightness.get());
                    value.setText("-50");
                    light.submitButton().doClick();
                    assertEquals(-50, brightness.get());
                });
    }

    @Test
    void translationAndLogKeepParameterOrderAndBounds() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    AtomicReference<int[]> translation = new AtomicReference<>();
                    TransladarView form = new TransladarView(translation::set);
                    JTextField x = (JTextField) PainelTest.named(form, "parameter.x"),
                            y = (JTextField) PainelTest.named(form, "parameter.y");
                    x.setText("abc");
                    y.setText("5");
                    form.submitButton().doClick();
                    assertNull(translation.get());
                    x.setText("-20");
                    form.submitButton().doClick();
                    assertArrayEquals(new int[] {-20, 5}, translation.get());
                    AtomicReference<Double> sigma = new AtomicReference<>();
                    AtomicInteger threshold = new AtomicInteger();
                    Marrhildrethview log =
                            new Marrhildrethview(
                                    (s, t) -> {
                                        sigma.set(s);
                                        threshold.set(t);
                                    });
                    java.util.List<JTextField> fields = new java.util.ArrayList<>();
                    collect(log, JTextField.class, fields::add);
                    fields.get(0).setText("0");
                    log.submitButton().doClick();
                    assertNull(sigma.get());
                    fields.get(0).setText("0,5");
                    fields.get(1).setText("17");
                    log.submitButton().doClick();
                    assertEquals(.5, sigma.get());
                    assertEquals(17, threshold.get());
                });
    }

    @Test
    void slidersAndMirrorKeepExistingSemantics() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    AtomicReference<Float> contrast = new AtomicReference<>();
                    ContrasteView form = new ContrasteView(contrast::set);
                    find(form, JSlider.class).setValue(50);
                    form.submitButton().doClick();
                    assertEquals(.5f, contrast.get());
                    AtomicInteger threshold = new AtomicInteger();
                    LimiarizacaoView binary = new LimiarizacaoView(threshold::set);
                    find(binary, JSlider.class).setValue(123);
                    binary.submitButton().doClick();
                    assertEquals(123, threshold.get());
                    AtomicReference<boolean[]> mirror = new AtomicReference<>();
                    EspelhamentoView flip = new EspelhamentoView(mirror::set);
                    java.util.List<JCheckBox> checks = new java.util.ArrayList<>();
                    collect(flip, JCheckBox.class, checks::add);
                    checks.get(0).setSelected(true);
                    flip.submitButton().doClick();
                    assertArrayEquals(new boolean[] {true, false}, mirror.get());
                    for (JCheckBox check : checks)
                        assertFalse(check.getAccessibleContext().getAccessibleName().isBlank());
                });
    }

    @Test
    void formFieldsHaveAssociatedLabels() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    ParameterForm form = new RotacionarView(p -> {});
                    java.util.List<JLabel> labels = new java.util.ArrayList<>();
                    collect(form, JLabel.class, labels::add);
                    assertTrue(
                            labels.stream()
                                    .anyMatch(
                                            l ->
                                                    "Ângulo".equals(l.getText())
                                                            && l.getLabelFor()
                                                                    instanceof JTextField));
                    assertTrue(
                            labels.stream()
                                    .anyMatch(
                                            l ->
                                                    "Direção".equals(l.getText())
                                                            && l.getLabelFor()
                                                                    instanceof JComboBox));
                    assertTrue(form.submitButton().isFocusable());
                });
    }

    private static JTextArea findError(Container form) {
        return (JTextArea) PainelTest.named(form, "form.error");
    }

    private static <T> T find(Container root, Class<T> type) {
        AtomicReference<T> result = new AtomicReference<>();
        collect(
                root,
                type,
                value -> {
                    if (result.get() == null) result.set(value);
                });
        return result.get();
    }

    private static <T> void collect(Container root, Class<T> type, Consumer<T> consumer) {
        for (Component c : root.getComponents()) {
            if (type.isInstance(c)) consumer.accept(type.cast(c));
            if (c instanceof Container child) collect(child, type, consumer);
        }
    }
}
