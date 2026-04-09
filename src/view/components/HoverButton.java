package view.components;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

/**
 * A custom button that provides a soft red glow and border on hover.
 * This distinguishes destructive actions from primary blue UI elements.
 */
public class HoverButton extends JButton {
    private boolean isHovered = false;
    
    /**
     * Using a very low alpha (30) for the background glow.
     * This prevents the red from being overwhelming.
     */
    private final Color hoverBg = new Color(220, 53, 69, 30);
    
    /**
     * The border matches the general 1.5f stroke weight used in the app's focus rings.
     */
    private final Color hoverBorder = new Color(220, 53, 69, 120);

    public HoverButton() {
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusable(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (isHovered) {
            Graphics2D g2d = (Graphics2D) g.create();
            // Match the high-quality rendering used in SongLinePanel
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 1. Paint the soft background glow
            g2d.setColor(hoverBg);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            
            // 2. Paint the rounded border
            g2d.setColor(hoverBorder);
            g2d.setStroke(new BasicStroke(1.5f));
            // Offset by 1px to ensure the stroke isn't clipped by the component bounds
            g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
            
            g2d.dispose();
        }
        super.paintComponent(g);
    }
}