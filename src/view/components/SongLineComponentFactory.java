package view.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * Factory class responsible for generating reusable UI components and visual properties 
 * specific to the SongLine grid. 
 *
 * Extracting this logic adheres to the Single Responsibility Principle, keeping 
 * the main SongLinePanel focused solely on layout and data orchestration.
 */
public class SongLineComponentFactory {

    /**
     * Standardized dimension for square icon buttons to maintain a consistent grid.
     */
    private static final Dimension ICON_BOX = new Dimension(40, 40);

    /**
     * Generates a fully styled removal button with flat aesthetics and custom hover states.
     *
     * @param onAction The callback to execute when the button is clicked.
     * @return A configured HoverButton instance.
     */
    public static HoverButton createRemoveButton(Runnable onAction) {
        HoverButton removeButton = new HoverButton();
        removeButton.setIcon(new TrashIcon(30, 35, new Color(180, 40, 55)));
        removeButton.setToolTipText("Remove this line");
        removeButton.setFocusable(false);
        
        // Strip out the default Swing button background/border to keep it flat
        removeButton.setContentAreaFilled(false);
        removeButton.setBorderPainted(false);
        removeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        removeButton.setMargin(new Insets(0, 0, 0, 0));
        removeButton.setBorder(BorderFactory.createEmptyBorder());
        removeButton.putClientProperty("JComponent.minimumWidth", 0);
        
        removeButton.setPreferredSize(ICON_BOX);
        removeButton.setMinimumSize(ICON_BOX);
        removeButton.setMaximumSize(ICON_BOX);
        
        if (onAction != null) {
            removeButton.addActionListener(e -> onAction.run());
        }
        
        return removeButton;
    }

    /**
     * Generates the drag handle icon label for reordering song lines.
     *
     * @return A configured JLabel containing the drag handle UI.
     */
    public static JLabel createDragHandle() {
        JLabel dragHandleLabel = new JLabel();
        dragHandleLabel.setName("dragHandle");
        dragHandleLabel.setIcon(new DragHandleIcon(30, 35, new Color(100, 100, 100)));
        dragHandleLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dragHandleLabel.setToolTipText("Click and drag to reorder this line");
        
        dragHandleLabel.setPreferredSize(ICON_BOX);
        dragHandleLabel.setHorizontalAlignment(JLabel.CENTER);
        dragHandleLabel.setMinimumSize(ICON_BOX);
        dragHandleLabel.setMaximumSize(ICON_BOX);
        
        return dragHandleLabel;
    }

    /**
     * Creates the default rounded border for unfocused song panels.
     *
     * @return The unfocused Border instance.
     */
    public static Border createDefaultBorder() {
        return new EmptyBorder(10, 10, 10, 10) {
            @Override
            public void paintBorder(java.awt.Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(70, 75, 80));
                g2d.drawRoundRect(x, y, width - 1, height - 1, 16, 16);
                g2d.dispose();
            }
        };
    }

    /**
     * Creates the highlighted rounded border for actively focused song panels.
     *
     * @return The focused Border instance.
     */
    public static Border createFocusedBorder() {
        return new EmptyBorder(10, 10, 10, 10) {
            @Override
            public void paintBorder(java.awt.Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color focusColor = UIManager.getColor("Component.focusColor");
                g2d.setColor(focusColor != null ? focusColor : new Color(62, 134, 224));
                g2d.setStroke(new java.awt.BasicStroke(2.0f));
                g2d.drawRoundRect(x + 1, y + 1, width - 3, height - 3, 16, 16);
                g2d.dispose();
            }
        };
    }
}