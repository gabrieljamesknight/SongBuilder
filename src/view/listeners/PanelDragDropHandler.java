package view.listeners;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import view.components.DragGlassPane;
import view.components.SongLinePanel;

/**
 * Handles drag-and-drop reordering of SongLinePanels with visual ghosting.
 * Utilizes a threshold check and in-place placeholder rendering to ensure 
 * mouse capture is never dropped by the OS.
 */
public class PanelDragDropHandler extends MouseAdapter {
    private final JPanel container;
    private final List<SongLinePanel> panelList;
    private final Runnable onReorderComplete;

    private SongLinePanel draggedPanel = null;
    private int initialIndex = -1;
    
    private Point startPoint = null;
    private boolean isDragging = false;
    private Point dragOffset;
    private DragGlassPane glassPane;

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
            startPoint = e.getPoint(); // Point is relative to the drag handle
            initialIndex = panelList.indexOf(draggedPanel);
            isDragging = false;
        }
    }

    private void initiateDrag(MouseEvent e) {
        isDragging = true;
        Component handle = e.getComponent();

        // 1. Calculate offset relative to the panel to keep the mouse anchored correctly
        Point panelLocation = draggedPanel.getLocation();
        Point clickPoint = SwingUtilities.convertPoint(handle, startPoint, container);
        dragOffset = new Point(clickPoint.x - panelLocation.x, clickPoint.y - panelLocation.y);

        // 2. Generate the visual snapshot BEFORE hiding elements
        BufferedImage dragImage = new BufferedImage(draggedPanel.getWidth(), draggedPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dragImage.createGraphics();
        draggedPanel.paint(g2d);
        g2d.dispose();

        // 3. Setup the floating Glass Pane
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(container);
        if (!(topFrame.getGlassPane() instanceof DragGlassPane)) {
            glassPane = new DragGlassPane();
            topFrame.setGlassPane(glassPane);
        } else {
            glassPane = (DragGlassPane) topFrame.getGlassPane();
        }
        
        glassPane.setDragImage(dragImage);
        updateGlassPaneLocation(e);
        glassPane.setVisible(true);

        // 4. Convert the panel into a dashed drop-zone
        draggedPanel.setPlaceholderMode(true);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (draggedPanel == null) return;

        // Threshold check: prevent accidental drags if clicked without moving
        if (!isDragging) {
            if (startPoint.distance(e.getPoint()) > 3) {
                initiateDrag(e);
            } else {
                return;
            }
        }

        updateGlassPaneLocation(e);

        // Calculate dynamic reordering
        Point containerPoint = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), container);
        int currentIndex = panelList.indexOf(draggedPanel);
        int targetIndex = currentIndex;

        for (int i = 0; i < panelList.size(); i++) {
            if (i == currentIndex) continue;

            SongLinePanel panel = panelList.get(i);
            int panelCenterY = panel.getY() + (panel.getHeight() / 2);
            
            if (currentIndex < i && containerPoint.y > panelCenterY) {
                targetIndex = i;
            } else if (currentIndex > i && containerPoint.y < panelCenterY) {
                targetIndex = i;
                break; 
            }
        }

        // Apply structural layout shift
        if (targetIndex != currentIndex) {
            panelList.remove(currentIndex);
            panelList.add(targetIndex, draggedPanel);
            
            container.removeAll();
            for (int i = 0; i < panelList.size(); i++) {
                if (i > 0) container.add(Box.createVerticalStrut(20));
                container.add(panelList.get(i));
            }
            container.revalidate();
            container.repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (draggedPanel == null) return;

        if (isDragging) {
            // Cleanup Glass Pane and restore original panel styling
            if (glassPane != null) {
                glassPane.setVisible(false);
                glassPane.setDragImage(null);
            }
            draggedPanel.setPlaceholderMode(false);

            int finalIndex = panelList.indexOf(draggedPanel);
            if (initialIndex != finalIndex && onReorderComplete != null) {
                onReorderComplete.run();
            }
        }

        // Reset state parameters
        draggedPanel = null;
        initialIndex = -1;
        isDragging = false;
        startPoint = null;
    }

    private void updateGlassPaneLocation(MouseEvent e) {
        Point screenPoint = e.getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(screenPoint, glassPane);
        glassPane.setImageLocation(new Point(screenPoint.x - dragOffset.x, screenPoint.y - dragOffset.y));
    }
}