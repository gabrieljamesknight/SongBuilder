import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.awt.Graphics;

public class CheckWidth {
    public static void main(String[] args) {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        Font f = new Font("Monospaced", Font.PLAIN, 16);
        FontMetrics fm = g.getFontMetrics(f);
        System.out.println("Char width: " + fm.charWidth('A'));
    }
}
