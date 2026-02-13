package main;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import com.formdev.flatlaf.FlatDarkLaf;
import view.SongBuilderGUI;

/**
 * The main entry point for the SongBuilder application.
 * Configures the UI Look and Feel and launches the main GUI window.
 */
public class SongBuilderApp {

    public static void main(String[] args) {
        // 1. Try to apply the Dark Theme
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize Dark Mode. Is the FlatLaf jar in your lib folder?");
            ex.printStackTrace();
        }

        // 2. Launch the Application
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    new SongBuilderGUI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}