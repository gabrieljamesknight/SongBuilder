package view.listeners;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import view.components.SongLinePanel;

/**
 * Handles drag-and-drop reordering of SongLinePanels within their container.
 * Attach this listener to the drag handle of each SongLinePanel.
 */
public class PanelDragDropHandler extends MouseAdapter {
    private final JPanel container;
    private final List<SongLinePanel> panelList;
    private final Runnable onReorderComplete;

    private SongLinePanel draggedPanel = null;
    private int initialIndex = -1;

    /**
     * Constructs the drag-and-drop handler.
     *
     * @param container         The JPanel containing the list of SongLinePanels.
     * @param panelList         The underlying ArrayList of panels reflecting the visual order.
     * @param onReorderComplete A callback triggered when a drop finishes and the order has changed.
     */
    public PanelDragDropHandler(JPanel container, List<SongLinePanel> panelList, Runnable onReorderComplete) {
        this.container = container;
        this.panelList = panelList;
        this.onReorderComplete = onReorderComplete;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Component handle = e.getComponent();
        draggedPanel = (SongLinePanel) SwingUtilities.getAncestorOfClass(SongLinePanel.class, handle);
        if (draggedPanel != null) {
            initialIndex = panelList.indexOf(draggedPanel);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (draggedPanel == null) return;

        // Convert the mouse point to the parent container's coordinate space
        Point containerPoint = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), container);
        
        int currentIndex = panelList.indexOf(draggedPanel);
        int targetIndex = currentIndex;

        // Determine if the mouse has crossed the center point of another panel
        for (int i = 0; i < panelList.size(); i++) {
            SongLinePanel panel = panelList.get(i);
            if (panel != draggedPanel) {
                int panelCenterY = panel.getY() + (panel.getHeight() / 2);
                if (currentIndex < i && containerPoint.y > panelCenterY) {
                    targetIndex = i;
                } else if (currentIndex > i && containerPoint.y < panelCenterY) {
                    targetIndex = i;
                    break; // Stop at the first panel we are above
                }
            }
        }

        // If the target has changed, visually swap them immediately
        if (targetIndex != currentIndex && targetIndex >= 0 && targetIndex < panelList.size()) {
            panelList.remove(currentIndex);
            panelList.add(targetIndex, draggedPanel);

            container.removeAll();
            for (int i = 0; i < panelList.size(); i++) {
                if (i > 0) {
                    container.add(Box.createVerticalStrut(20));
                }
                container.add(panelList.get(i));
            }
            
            container.revalidate();
            container.repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (draggedPanel != null) {
            int finalIndex = panelList.indexOf(draggedPanel);
            // Trigger the callback if the position actually changed
            if (initialIndex != finalIndex && onReorderComplete != null) {
                onReorderComplete.run();
            }
            draggedPanel = null;
            initialIndex = -1;
        }
    }
}