package view.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.Icon;

/**
 * A scalable, minimalist flat-design trash bin icon rendered using Java2D.
 * Utilizes a streamlined Path2D structure to create a clean, modern aesthetic.
 */
public class TrashIcon implements Icon {
    private final int width;
    private final int height;
    private final Color color;

    /**
     * Constructs a TrashIcon with specified dimensions and color.
     *
     * @param width  The width of the icon in pixels.
     * @param height The height of the icon in pixels.
     * @param color  The flat color applied to the icon strokes.
     */
    public TrashIcon(int width, int height, Color color) {
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

        // Bold, minimalist 2.5f stroke
        g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // 1. Draw the lid (straight horizontal line)
        int lidY = y + (int) (height * 0.25);
        g2d.drawLine(x + 2, lidY, x + width - 2, lidY);

        // 2. Draw the top handle
        int handleWidth = width / 3;
        int handleX = x + (width - handleWidth) / 2;
        int handleHeight = height / 5;
        int handleY = lidY - (handleHeight / 2) - 1;
        g2d.drawArc(handleX, handleY, handleWidth, handleHeight, 0, 180);

        // 3. Draw the perfect "U" shaped body using Cubic Beziers
        Path2D.Float body = new Path2D.Float();
        
        float bodyTopY = lidY + 4;
        float bodyBottomY = y + height - 2;
        
        float leftTopX = x + width * 0.15f;
        float rightTopX = x + width * 0.85f;
        
        float leftBottomX = x + width * 0.25f;
        float rightBottomX = x + width * 0.75f;
        
        /**
         * Calculate the exact radius to make the bottom a perfect semi-circle.
         * We use the Bezier approximation constant (kappa = 0.55228f) to 
         * draw a flawlessly smooth circular arc at the bottom of the bin.
         */
        float radius = (rightBottomX - leftBottomX) / 2.0f;
        float curveStartY = bodyBottomY - radius;
        float centerX = (leftBottomX + rightBottomX) / 2.0f;
        float offset = radius * 0.55228f;

        // Trace the path
        body.moveTo(leftTopX, bodyTopY);               // Top-left point
        body.lineTo(leftBottomX, curveStartY);         // Left angled wall
        
        // Left half of the bottom 'U' curve
        body.curveTo(
            leftBottomX, curveStartY + offset,         // Control point 1
            centerX - offset, bodyBottomY,             // Control point 2
            centerX, bodyBottomY                       // End point (bottom center)
        );
        
        // Right half of the bottom 'U' curve
        body.curveTo(
            centerX + offset, bodyBottomY,             // Control point 1
            rightBottomX, curveStartY + offset,        // Control point 2
            rightBottomX, curveStartY                  // End point (start of right wall)
        );
        
        // Right angled wall back up to top-right
        body.lineTo(rightTopX, bodyTopY);

        g2d.draw(body);
        g2d.dispose();
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }
}