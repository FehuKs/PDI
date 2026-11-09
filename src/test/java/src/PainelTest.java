package src;

import java.awt.*;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfSystemProperty(named="java.awt.headless", matches="false")
class PainelTest {
    @TempDir Path dir;
    @Test void loadProcessUndoResetAndCapture() throws Exception {
        var source=Fixtures.shape("square",Color.RED);var path=dir.resolve("quadrado.png");ImageIO.write(source,"png",path.toFile());
        AtomicReference<Painel> frame=new AtomicReference<>();
        SwingUtilities.invokeAndWait(()->{var p=new Painel();p.setExtendedState(JFrame.NORMAL);p.setSize(1100,720);p.setLocation(20,20);p.setVisible(true);frame.set(p);p.carregarImagem(path);assertFalse(p.getJMenuBar().getMenu(2).isEnabled());});
        Painel p=frame.get();
        try {
            await(()->status(p).contains("quadrado.png") && status(p).contains("0 operação"));
            SwingUtilities.invokeAndWait(()->p.getJMenuBar().getMenu(2).getItem(0).doClick());
            await(()->status(p).contains("1 operação"));
            SwingUtilities.invokeAndWait(()->button(p,"Desfazer").doClick());
            await(()->status(p).contains("0 operação"));
            SwingUtilities.invokeAndWait(()->p.getJMenuBar().getMenu(2).getItem(0).doClick());
            await(()->status(p).contains("1 operação"));
            String screenshot=System.getProperty("pdi.screenshot");
            if(screenshot!=null){Rectangle bounds=new Rectangle();SwingUtilities.invokeAndWait(()->bounds.setBounds(p.getBounds()));ImageIO.write(new Robot().createScreenCapture(bounds),"png",Path.of(screenshot).toFile());}
            SwingUtilities.invokeAndWait(()->button(p,"Resetar").doClick());
            await(()->status(p).contains("0 operação"));
            SwingUtilities.invokeAndWait(()->assertFalse(button(p,"Desfazer").isEnabled()));
        } finally { SwingUtilities.invokeAndWait(p::dispose); }
    }
    private static void await(BooleanSupplier condition) throws Exception {
        long deadline=System.nanoTime()+10_000_000_000L;
        while(System.nanoTime()<deadline){AtomicReference<Boolean> ready=new AtomicReference<>(false);SwingUtilities.invokeAndWait(()->ready.set(condition.getAsBoolean()));if(ready.get())return;Thread.sleep(20);}
        fail("A interface não concluiu a operação em 10 segundos.");
    }
    private static String status(Container parent){
        for(Component c:parent.getComponents()){if(c instanceof JLabel label && (label.getText().contains("px") || label.getText().contains("Carregando")))return label.getText();if(c instanceof Container nested){String text=status(nested);if(!text.isEmpty())return text;}}
        return "";
    }
    private static AbstractButton button(Container parent,String text){
        for(Component c:parent.getComponents()){if(c instanceof AbstractButton b && b.getText()!=null && b.getText().contains(text))return b;if(c instanceof Container nested){AbstractButton b=button(nested,text);if(b!=null)return b;}}
        return null;
    }
}
