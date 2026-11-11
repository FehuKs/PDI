package src;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import src.filtros.*;
import src.transformacoes.*;
import src.ui.*;

import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.*;

@EnabledIfSystemProperty(named = "java.awt.headless", matches = "false")
class DialogsDesktopTest {
    @Test
    void parameterDialogsKeepActionsVisibleOnSmallScreensAndCancelSafely() throws Exception {
        SwingUtilities.invokeAndWait(
                () -> {
                    Theme.install();
                    JFrame owner = new JFrame();
                    owner.setSize(760, 520);
                    owner.setLocation(0, 0);
                    owner.setVisible(true);
                    AtomicInteger calls = new AtomicInteger();
                    try {
                        List<ParameterForm> forms =
                                List.of(
                                        new BrilhoView(v -> calls.incrementAndGet()),
                                        new EscalaView(v -> calls.incrementAndGet()),
                                        new RotacionarView(v -> calls.incrementAndGet()),
                                        new TransladarView(v -> calls.incrementAndGet()),
                                        new EspelhamentoView(v -> calls.incrementAndGet()),
                                        new ContrasteView(v -> calls.incrementAndGet()),
                                        new LimiarizacaoView(v -> calls.incrementAndGet()),
                                        new Marrhildrethview((s, t) -> calls.incrementAndGet()));
                        for (ParameterForm form : forms) {
                            JDialog dialog = Dialogs.parameters(owner, "Parâmetros", form);
                            assertTrue(dialog.isModal());
                            dialog.setModal(false);
                            dialog.setVisible(true);
                            try {
                                form.showError(
                                        "Digite um valor válido para continuar. As alterações ainda"
                                                + " não foram aplicadas.");
                                Rectangle screen = dialog.getGraphicsConfiguration().getBounds();
                                assertTrue(
                                        screen.contains(dialog.getBounds()),
                                        "Dialog fits the display");
                                JButton apply = form.submitButton();
                                Rectangle bounds =
                                        SwingUtilities.convertRectangle(
                                                apply.getParent(),
                                                apply.getBounds(),
                                                dialog.getContentPane());
                                assertTrue(
                                        new Rectangle(dialog.getContentPane().getSize())
                                                .contains(bounds),
                                        "Submit remains visible");
                                AbstractButton cancel =
                                        (AbstractButton) PainelTest.named(dialog, "form.cancel");
                                assertNotNull(cancel);
                                cancel.doClick();
                                assertFalse(dialog.isShowing());
                                assertEquals(0, calls.get());
                            } finally {
                                dialog.dispose();
                            }
                        }
                    } finally {
                        owner.dispose();
                    }
                });
    }
}
