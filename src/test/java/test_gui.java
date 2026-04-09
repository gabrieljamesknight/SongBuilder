import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class test_gui {
    public static void main(String[] args) throws Exception {
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
                public void mousePressed(MouseEvent e) {
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
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }
}
