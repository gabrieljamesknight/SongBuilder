package main;

import javax.swing.SwingUtilities;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.formdev.flatlaf.FlatDarkLaf;
import view.SongBuilderGUI;

/**
 * The main entry point for the SongBuilder application.
 * Configures the UI Look and Feel and launches the main GUI window.
 */
public class SongBuilderApp {
    
    // Professional practice: Use a Logger instead of standard error streams
    private static final Logger LOGGER = Logger.getLogger(SongBuilderApp.class.getName());

    public static void main(String[] args) {
        // 1. Try to apply the Dark Theme using FlatLaf's modern setup method
        try {
            FlatDarkLaf.setup();
        } catch (Exception ex) {
            // Logging at SEVERE level automatically prints the stack trace properly, clearing the hint
            LOGGER.log(Level.SEVERE, "Failed to initialize Dark Mode. Falling back to default UI.", ex);
        }

        // 2. Launch the Application
        SwingUtilities.invokeLater(() -> {
            try {
                @SuppressWarnings("unused")
                SongBuilderGUI gui = new SongBuilderGUI();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Fatal error initializing SongBuilder GUI:", e);
            }
        });
    }
}