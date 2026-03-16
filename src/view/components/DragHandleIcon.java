package view.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

/**
 * A scalable, flat-design drag handle (hamburger) icon rendered using Java2D.
 * Ensures perfectly symmetrical line spacing across all operating systems,
 * bypassing typographic rendering inconsistencies.
 */
public class DragHandleIcon implements Icon {
    private final int width;
    private final int height;
    private final Color color;

    /**
     * Constructs a DragHandleIcon with specified dimensions and color.
     *
     * @param width  The width of the icon in pixels.
     * @param height The height of the icon in pixels.
     * @param color  The flat color applied to the icon strokes.
     */
    public DragHandleIcon(int width, int height, Color color) {
        this.width = width;
        this.height = height;
        this.color = color;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();
        
        // Enable anti-aliasing for smooth, professional curves
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);
        
        // Use a rounded stroke for a modern, tactile feel
        g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // Calculate exact spacing to ensure the three lines are perfectly equidistant
        int gap = height / 3;
        int startY = y + (height - (gap * 2)) / 2;

        g2d.drawLine(x + 2, startY, x + width - 2, startY);
        g2d.drawLine(x + 2, startY + gap, x + width - 2, startY + gap);
        g2d.drawLine(x + 2, startY + gap * 2, x + width - 2, startY + gap * 2);

        g2d.dispose();
    }

    @Override
    public int getIconWidth() { return width; }

    @Override
    public int getIconHeight() { return height; }
}