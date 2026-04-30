import javax.swing.*;
import java.awt.*;

public class test_spinner {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            JSpinner sp = new JSpinner(new SpinnerNumberModel(120, 30, 300, 1));
            JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) sp.getEditor();
            
            // Fix attempt 1
            int[] lastCaret = {0};
            editor.getTextField().addCaretListener(e -> {
                // If it's a programmatic change, it might jump to 0. 
                // We want to record user caret position.
                // Actually, let's just print it.
                System.out.println("Caret changed: " + e.getDot());
            });
            sp.addChangeListener(e -> {
                System.out.println("Spinner changed");
                int caretPos = editor.getTextField().getCaretPosition();
                System.out.println("Caret at change: " + caretPos);
            });

            frame.add(sp);
            frame.pack();
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            // simulate up arrow
            try { Thread.sleep(1000); } catch(Exception e){}
            sp.setValue(121);
            try { Thread.sleep(500); } catch(Exception e){}
            System.exit(0);
        });
    }
}
