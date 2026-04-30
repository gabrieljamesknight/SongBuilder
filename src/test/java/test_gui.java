package test.java;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Automated test environment for validating mouse interactions 
 * and caret alignment within standard text fields.
 */
public class test_gui {

    private static final Logger LOGGER = Logger.getLogger(test_gui.class.getName());

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            
            JTextField tf = new JTextField("4/4");
            tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            tf.setHorizontalAlignment(JTextField.LEFT);
            tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 62, 66)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
            ));
            
            tf.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    // Left as standard out intentionally as this is an explicit testing probe
                    System.out.println("Clicked at x=" + e.getX() + ", caret=" + tf.getCaretPosition());
                }
            });
            
            panel.add(tf);
            frame.add(panel);
            frame.setSize(300, 200);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    Robot robot = new Robot();
                    Point p = tf.getLocationOnScreen();
                    robot.mouseMove(p.x + 200, p.y + 10);
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                    Thread.sleep(500);
                    
                    System.out.println("Final caret position: " + tf.getCaretPosition());
                    System.exit(0);
                } catch (AWTException | InterruptedException e) {
                    LOGGER.log(Level.SEVERE, "Automation thread interrupted or Robot failed to execute.", e);
                    // Safely restore the interrupted status
                    Thread.currentThread().interrupt(); 
                }
            }).start();
        });
    }
}