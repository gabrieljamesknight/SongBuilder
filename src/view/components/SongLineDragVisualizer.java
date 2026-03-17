package view.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * Extracts drag-and-drop placeholder visual state management 
 * out of the SongLinePanel to adhere strictly to the Single Responsibility Principle.
 */
public class SongLineDragVisualizer {
    
    private Dimension normalSize = null;
    private final SongLinePanel panel;
    private final FocusAwareBorderPanel innerContentPanel;
    private final JLabel dragHandleLabel;
    private final JTextField sectionLabelField;

    public SongLineDragVisualizer(SongLinePanel panel, 
                                  FocusAwareBorderPanel innerContentPanel, 
                                  JLabel dragHandleLabel, 
                                  JTextField sectionLabelField) {
        this.panel = panel;
        this.innerContentPanel = innerContentPanel;
        this.dragHandleLabel = dragHandleLabel;
        this.sectionLabelField = sectionLabelField;
    }

    public void setPlaceholderMode(boolean active) {
        if (active) {
            normalSize = panel.getSize();
            panel.setPreferredSize(normalSize);
            panel.setMinimumSize(normalSize);
            
            for (Component c : innerContentPanel.getComponents()) {
                if (c != dragHandleLabel) {
                    c.setVisible(false);
                }
            }
            sectionLabelField.setVisible(false);
            innerContentPanel.setBorder(BorderFactory.createDashedBorder(new Color(100, 130, 200), 3, 5, 2, false));
        } else {
            for (Component c : innerContentPanel.getComponents()) {
                c.setVisible(true);
            }
            sectionLabelField.setVisible(true);
            
            Dimension fixedDimension = new Dimension(850, 280);
            panel.setPreferredSize(fixedDimension);
            panel.setMinimumSize(fixedDimension);
            innerContentPanel.setPlaceholderMode(false);
        }
        panel.revalidate();
        panel.repaint();
    }
}