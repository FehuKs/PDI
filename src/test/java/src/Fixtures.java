package src;

import java.awt.*;
import java.awt.image.BufferedImage;

final class Fixtures {
    static BufferedImage solid(int type, int rgb) {
        BufferedImage image = new BufferedImage(7, 7, type);
        for (int y = 0; y < 7; y++) for (int x = 0; x < 7; x++) image.setRGB(x, y, rgb);
        return image;
    }
    static BufferedImage canvas(int w, int h) {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics(); g.setColor(Color.WHITE); g.fillRect(0, 0, w, h); g.dispose(); return image;
    }
    static BufferedImage shape(String shape, Color color) {
        BufferedImage image = canvas(220, 180); Graphics2D g = image.createGraphics(); g.setColor(color);
        switch (shape) {
            case "square" -> g.fillRect(60, 40, 80, 80);
            case "circle" -> g.fillOval(50, 30, 100, 100);
            case "rectangle" -> g.fillRect(40, 50, 140, 60);
            case "triangle" -> g.fillPolygon(new int[]{110, 40, 180}, new int[]{30, 140, 140}, 3);
        }
        g.dispose(); return image;
    }
    static BufferedImage letter(char value) {
        BufferedImage image = canvas(200, 200); Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new Font("SansSerif", Font.BOLD, 160)); g.setColor(Color.BLACK);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(String.valueOf(value), (200-fm.charWidth(value))/2, (200-fm.getHeight())/2+fm.getAscent());
        g.dispose(); return image;
    }
    static BufferedImage clock(int hour, int minute) {
        BufferedImage image = canvas(300, 300); Graphics2D g = image.createGraphics(); g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(4)); g.drawOval(30,30,240,240);
        hand(g, (hour%12)*30+minute*0.5,70); hand(g, minute*6,100);g.dispose();return image;
    }
    private static void hand(Graphics2D g, double angle, int length) {
        double radians = Math.toRadians(angle);g.drawLine(150,150,150+(int)Math.round(Math.sin(radians)*length),150-(int)Math.round(Math.cos(radians)*length));
    }
    static BufferedImage bars(boolean labels) { return bars(labels ? "10" : null, labels ? "20" : null); }
    static BufferedImage bars(String low, String high) {
        BufferedImage image = canvas(360,260);Graphics2D g=image.createGraphics();
        g.setColor(Color.RED);g.fillRect(70,140,50,80);g.setColor(Color.BLUE);g.fillRect(220,80,50,140);
        if(low != null && high != null){g.setColor(Color.BLACK);g.setFont(new Font("SansSerif",Font.BOLD,32));g.drawString(low,75,130);g.drawString(high,225,70);}
        g.dispose();return image;
    }
}
