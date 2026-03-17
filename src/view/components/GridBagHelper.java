package view.components;

import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * A utility class designed to reduce boilerplate code when configuring 
 * GridBagConstraints for Swing layouts.
 */
public class GridBagHelper {

    /**
     * Generates a standard GridBagConstraints object with predefined defaults 
     * for the SongBuilder grid layout.
     *
     * @param gridx   The column index.
     * @param gridy   The row index.
     * @param weightx The horizontal distribution weight.
     * @param insets  The margins applied to the component.
     * @param anchor  The directional anchor (e.g., GridBagConstraints.WEST).
     * @return A fully configured GridBagConstraints object.
     */
    public static GridBagConstraints createConstraints(int gridx, int gridy, double weightx, Insets insets, int anchor) {
        GridBagConstraints gbc = new GridBagConstraints();
        // Defaults for this specific UI structure
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        
        // Dynamic variables
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.weightx = weightx;
        gbc.insets = insets;
        gbc.anchor = anchor;
        
        return gbc;
    }
}