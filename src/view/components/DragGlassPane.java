package view.components;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * A transparent overlay used to render a floating "ghost" image of a component 
 * during a drag-and-drop operation.
 */
public class DragGlassPane extends JPanel {
    private BufferedImage dragImage;
    private Point location;

    public DragGlassPane() {
        setOpaque(false);
    }

    public void setDragImage(BufferedImage dragImage) {
        this.dragImage = dragImage;
    }

    public void setImageLocation(Point location) {
        this.location = location;
        repaint();
    }

    /**
     * Prevents the glass pane from intercepting mouse events, allowing the drag 
     * operation on the underlying components to continue seamlessly.
     */
    @Override
    public boolean contains(int x, int y) {
        return false; 
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (dragImage != null && location != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            // Apply 65% opacity for a professional "picked up" ghost effect
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));
            
            // Draw a subtle drop shadow
            g2d.setColor(new java.awt.Color(0, 0, 0, 50));
            g2d.fillRect(location.x + 5, location.y + 5, dragImage.getWidth(), dragImage.getHeight());
            
            // Draw the actual component image
            g2d.drawImage(dragImage, location.x, location.y, null);
            g2d.dispose();
        }
    }
}