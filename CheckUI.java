import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class CheckUI {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JLabel label = new JLabel();
            Font f = new Font("Monospaced", Font.PLAIN, 16);
            FontMetrics fm = label.getFontMetrics(f);
            System.out.println("Char width: " + fm.charWidth('A'));
            System.out.println("String width of 56 chars: " + fm.stringWidth("A".repeat(56)));
        });
    }
}
