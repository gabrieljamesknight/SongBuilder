package main;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatDarkLaf;

import controller.SongController;
import controller.SongManager;
import view.SongBuilderGUI;

/**
 * The main entry point for the SongBuilder application.
 * Configures the UI Look and Feel and launches the main GUI window.
 */
public class SongBuilderApp {
    
    // Use a Logger instead of standard error streams
    private static final Logger LOGGER = Logger.getLogger(SongBuilderApp.class.getName());

    public static void main(String[] args) {
        // 1. Try to apply the Dark Theme using FlatLaf's modern setup method
        try {
            // Apply global rounding to components to eliminate sharp corners
            javax.swing.UIManager.put("Button.arc", 12);
            javax.swing.UIManager.put("Component.arc", 12);
            javax.swing.UIManager.put("TextComponent.arc", 12);
            
            // Modernize the scrollbar to look like macOS/Windows 11
            javax.swing.UIManager.put("ScrollBar.thumbArc", 999);
            javax.swing.UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));

            FlatDarkLaf.setup();} catch (Exception ex) {
            // Logging at SEVERE level automatically prints the stack trace properly, clearing the hint
            LOGGER.log(Level.SEVERE, "Failed to initialize Dark Mode. Falling back to default UI.", ex);
        }

        // 2. Launch the Application
        SwingUtilities.invokeLater(() -> {
            try {
                SongManager manager = new SongManager();
                SongBuilderGUI gui = new SongBuilderGUI();
                @SuppressWarnings("unused")
                SongController controller = new SongController(gui, manager);
    
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Fatal error initializing SongBuilder GUI:", e);
            }
        });
    }
}