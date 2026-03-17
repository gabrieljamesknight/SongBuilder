package view.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

/**
 * A specialized JPanel that dynamically updates its border when any of its
 * descendant components gain or lose keyboard focus.
 */
public class FocusAwareBorderPanel extends JPanel {

    private final Border defaultBorder;
    private final Border focusedBorder;
    private boolean forceHighlight = false;
    private Runnable onFocusGainedAction;
    private boolean isPlaceholderActive = false;


    /**
     * Constructs the focus-aware panel.
     *
     * @param defaultBorder The border to display when unfocused.
     * @param focusedBorder The border to display when a child has focus.
     */
    public FocusAwareBorderPanel(Border defaultBorder, Border focusedBorder) {
        this.defaultBorder = defaultBorder;
        this.focusedBorder = focusedBorder;
        this.setBorder(defaultBorder);
        
        // Make the panel focusable to intercept clicks on empty space
        this.setFocusable(true);
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }

    /**
     * Toggles placeholder mode to prevent focus borders from overriding 
     * drag-and-drop visual states.
     */
    public void setPlaceholderMode(boolean active) {
        this.isPlaceholderActive = active;
        if (!active) {
            updateBorderState();
        }
    }

    public void setOnFocusGainedAction(Runnable action) {
        this.onFocusGainedAction = action;
    }

    public void setForceHighlight(boolean force) {
        this.forceHighlight = force;
        updateBorderState();
    }

    /**
     * Recursively attaches focus listeners to a component and its children.
     * Call this after adding interactive elements.
     */
    public void attachFocusTracking(Container container) {
        java.awt.event.FocusAdapter tracker = new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (onFocusGainedAction != null) onFocusGainedAction.run();
                updateBorderState();
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                javax.swing.SwingUtilities.invokeLater(() -> updateBorderState());
            }
        };

        // Ensure the root container itself is also tracked for focus events
        container.addFocusListener(tracker);
        attachToTree(container, tracker);
    }

    private void attachToTree(Container container, FocusAdapter tracker) {
        for (Component c : container.getComponents()) {
            c.addFocusListener(tracker);
            if (c instanceof Container) {
                attachToTree((Container) c, tracker);
            }
        }
    }

    private void updateBorderState() {
        // If we are currently a dashed placeholder, don't paint the focus ring
        if (isPlaceholderActive) return;

        java.awt.Component focusOwner = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        boolean hasFocus = forceHighlight || (focusOwner != null && javax.swing.SwingUtilities.isDescendingFrom(focusOwner, this));
        this.setBorder(hasFocus ? focusedBorder : defaultBorder);
        this.repaint();
    }
}