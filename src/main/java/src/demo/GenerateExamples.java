package src.demo;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;

/** Synthetic, reproducible examples; no external images or personal data. */
public final class GenerateExamples {
    private GenerateExamples() {}
    private static BufferedImage canvas(int width,int height) {
        BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=image.createGraphics();g.setColor(Color.WHITE);g.fillRect(0,0,width,height);g.dispose();return image;
    }
    public static void main(String[] args) throws Exception {
        Path dir=Path.of(args.length==0?"examples":args[0]);Files.createDirectories(dir);
        BufferedImage shapes=canvas(700,220);Graphics2D g=shapes.createGraphics();
        g.setColor(Color.RED);g.fillRect(30,60,80,80);g.setColor(Color.YELLOW);g.fillOval(180,50,100,100);
        g.setColor(Color.GREEN);g.fillRect(330,70,120,60);g.setColor(Color.BLUE);g.fillPolygon(new int[]{570,510,630},new int[]{40,150,150},3);g.dispose();save(dir,"formas.png",shapes);
        BufferedImage letters=canvas(620,200);g=letters.createGraphics();g.setColor(Color.BLACK);g.setFont(new Font("SansSerif",Font.BOLD,130));g.drawString("ABCD",50,145);g.dispose();save(dir,"letras.png",letters);
        for(int hour:new int[]{3,6}) {
            BufferedImage clock=canvas(300,300);g=clock.createGraphics();g.setColor(Color.BLACK);g.setStroke(new BasicStroke(4));g.drawOval(30,30,240,240);
            g.drawLine(150,150,150,50);double a=Math.toRadians(hour*30);g.drawLine(150,150,150+(int)Math.round(Math.sin(a)*70),150-(int)Math.round(Math.cos(a)*70));g.dispose();save(dir,"relogio-"+hour+".png",clock);
        }
        for(boolean labels:new boolean[]{true,false}) {
            BufferedImage bars=canvas(360,260);g=bars.createGraphics();g.setColor(Color.RED);g.fillRect(70,140,50,80);g.setColor(Color.BLUE);g.fillRect(220,80,50,140);
            if(labels){g.setColor(Color.BLACK);g.setFont(new Font("SansSerif",Font.BOLD,32));g.drawString("10",75,130);g.drawString("20",225,70);}g.dispose();save(dir,labels?"barras.png":"barras-sem-escala.png",bars);
        }
        BufferedImage stop=canvas(220,220);g=stop.createGraphics();g.setColor(Color.RED);int[] x=new int[8],y=new int[8];for(int n=0;n<8;n++){double a=Math.toRadians(22.5+n*45);x[n]=110+(int)(80*Math.cos(a));y[n]=110+(int)(80*Math.sin(a));}g.fillPolygon(x,y,8);g.dispose();save(dir,"pare.png",stop);
        for(Color color:new Color[]{Color.RED,Color.BLACK}){BufferedImage square=canvas(220,180);g=square.createGraphics();g.setColor(color);g.fillRect(60,40,80,80);g.dispose();save(dir,color==Color.RED?"quadrado-vermelho.png":"quadrado-preto.png",square);}
        System.out.println("Exemplos sintéticos gerados em "+dir.toAbsolutePath());
    }
    private static void save(Path dir,String filename,BufferedImage image)throws Exception {
        if(!ImageIO.write(image,"png",dir.resolve(filename).toFile()))throw new java.io.IOException("Codificador PNG indisponível.");
    }
}
