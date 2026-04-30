package test.java;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * UI scratchpad for testing text field caret positioning logic.
 */
public class TestSwing {
    public static void main(String[] args) {
        JTextField t = new JTextField();
        MouseAdapter clickFix = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                JTextField t = (JTextField) e.getSource();
                SwingUtilities.invokeLater(() -> {
                    if (t.getSelectionStart() == t.getSelectionEnd()) {
                        // Replaced deprecated viewToModel with high-DPI safe viewToModel2D
                        int offset = t.viewToModel2D(e.getPoint());
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
