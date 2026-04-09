import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TestSwing {
    public static void main(String[] args) {
        JTextField t = new JTextField();
        MouseAdapter clickFix = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                JTextField t = (JTextField) e.getSource();
                SwingUtilities.invokeLater(() -> {
                    if (t.getSelectionStart() == t.getSelectionEnd()) {
                        int offset = t.viewToModel(e.getPoint());
                        if (offset >= 0) {
                            t.setCaretPosition(offset);
                        }
                    }
                });
            }
        };
        t.addMouseListener(clickFix);
    }
}
