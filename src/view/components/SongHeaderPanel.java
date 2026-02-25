package view.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Encapsulates the top header of the application, including the song title input
 * and the primary action buttons (Add, Save, Load).
 */
public class SongHeaderPanel extends JPanel {

    private final JTextField songNameField;

    /**
     * Constructs the header panel and injects the necessary action callbacks.
     *
     * @param onAddLine Action to trigger when adding a new song line.
     * @param onSaveSong Action to trigger when saving the song.
     * @param onLoadSong Action to trigger when loading a song.
     */
    public SongHeaderPanel(Runnable onAddLine, Runnable onSaveSong, Runnable onLoadSong) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Song Name Field Styling
        songNameField = new JTextField(20);
        songNameField.setFont(new Font("Arial", Font.BOLD, 24));
        songNameField.setHorizontalAlignment(JTextField.CENTER);
        songNameField.setBackground(new Color(60, 60, 60));
        songNameField.setForeground(Color.WHITE);
        songNameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5) 
        ));
        
        Dimension songNameFieldDim = new Dimension(400, 40); 
        songNameField.setPreferredSize(songNameFieldDim);
        songNameField.setMaximumSize(songNameFieldDim);

        // Main Interaction Buttons
        Font font = new Font("Arial", Font.PLAIN, 16);
        
        JButton addLineButton = new JButton("Add Line");
        addLineButton.setFont(font);
        addLineButton.addActionListener(e -> onAddLine.run());

        JButton saveSongButton = new JButton("Save Song");
        saveSongButton.setFont(font);
        saveSongButton.addActionListener(e -> onSaveSong.run());
        
        JButton loadSongButton = new JButton("Load Song");
        loadSongButton.setFont(font);
        loadSongButton.addActionListener(e -> onLoadSong.run());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(addLineButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(saveSongButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(loadSongButton);

        JLabel songNameLabel = new JLabel("Song Name:");
        songNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        add(songNameLabel);
        add(songNameField);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(buttonPanel);
    }

    /**
     * Retrieves the current text from the song name field.
     *
     * @return The song name.
     */
    public String getSongName() {
        return songNameField.getText();
    }

    /**
     * Updates the song name field text.
     *
     * @param name The new song name to display.
     */
    public void setSongName(String name) {
        songNameField.setText(name);
    }
    
    /**
     * Exposes the ActionMap for the text field to support copy/paste in the main menu.
     * * @return The ActionMap of the song name text field.
     */
    public javax.swing.ActionMap getTextFieldActionMap() {
        return songNameField.getActionMap();
    }
}