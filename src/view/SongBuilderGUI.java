package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import model.Song;
import model.SongLine;
import view.components.SongBuilderMenuBar;
import view.components.SongLinePanel;

/**
 * The main graphical user interface for the SongBuilder application.
 * Operates strictly as a View component within the MVC architecture. 
 * Handles rendering of the UI, tablature grids, and captures user input to pass to the Controller.
 */
public class SongBuilderGUI {
    
    private JFrame frame;
    private JTextField songNameField;
    private JButton addLineButton;
    private JButton saveSongButton;
    private JButton loadSongButton;
    private ArrayList<SongLinePanel> songLinePanels;
    private JTextField[] tuningFields;
    private JScrollPane scrollPane;
    private JPanel songLinePanelContainer;

    // Action Callbacks (injected by the Controller)
    private Runnable newSongAction = () -> {};
    private Runnable saveSongAction = () -> {};
    private Runnable saveSongAsAction = () -> {};
    private Runnable loadSongAction = () -> {};
    private Consumer<Integer> removeLineCallback = (index) -> {};

    /**
     * Initializes the GUI components and creates the initial empty song state.
     */
    public SongBuilderGUI() {
        songLinePanels = new ArrayList<>();
        tuningFields = new JTextField[6];
        
        // Initialize tuning fields with empty strings or default standard tuning spaces
        for (int i = 0; i < 6; i++) {
            tuningFields[i] = new JTextField(" ", 2);
        }
        
        setupUI();
    }

    // --- Callback Setters ---

    public void setNewSongAction(Runnable action) { 
        this.newSongAction = action; 
        updateMenuBar(); 
    }
    
    public void setSaveSongAction(Runnable action) { 
        this.saveSongAction = action; 
        updateMenuBar();
    }
    
    public void setSaveSongAsAction(Runnable action) { 
        this.saveSongAsAction = action; 
        updateMenuBar(); 
    }
    
    public void setLoadSongAction(Runnable action) { 
        this.loadSongAction = action; 
        updateMenuBar();
    }
    
    public void setRemoveLineCallback(Consumer<Integer> callback) { 
        this.removeLineCallback = callback; 
    }

    // --- UI Setup ---

    /**
     * Constructs the main frame, layout constraints, and visual components.
     */
    private void setupUI() {
        frame = new JFrame("SongBuilder");
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.setPreferredSize(new Dimension(850, 700));

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

        Font font = new Font("Arial", Font.PLAIN, 16);
        
        // Main Interaction Buttons
        addLineButton = new JButton("Add Line");
        addLineButton.setFont(font);
        saveSongButton = new JButton("Save Song");
        saveSongButton.setFont(font);
        loadSongButton = new JButton("Load Song");
        loadSongButton.setFont(font);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(addLineButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(saveSongButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(loadSongButton);

        frame.add(Box.createRigidArea(new Dimension(0, 10)));
        JLabel songNameLabel = new JLabel("Song Name:");
        songNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        frame.add(songNameLabel);
        frame.add(songNameField);
        
        frame.add(Box.createRigidArea(new Dimension(0, 10)));
        frame.add(buttonPanel);
        frame.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Song Line Container (Scrollable area for tablature)
        songLinePanelContainer = new JPanel();
        songLinePanelContainer.setLayout(new BoxLayout(songLinePanelContainer, BoxLayout.Y_AXIS));
        songLinePanelContainer.setBorder(new EmptyBorder(5, 5, 45, 5));

        scrollPane = new JScrollPane(songLinePanelContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);    
        frame.add(scrollPane, BorderLayout.CENTER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        
        // Action Listeners for View-level buttons triggering Controller callbacks
        addLineButton.addActionListener(e -> addLineAction());
        saveSongButton.addActionListener(e -> saveSongAction.run());
        loadSongButton.addActionListener(e -> loadSongAction.run());

        updateMenuBar();

        // Add Initial Panel
        addLineAction();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(true); 
    }

    /**
     * Initializes or refreshes the JMenuBar with the latest action callbacks.
     */
    private void updateMenuBar() {
        SongBuilderMenuBar menuBar = new SongBuilderMenuBar(
            songNameField.getActionMap(),
            newSongAction,
            saveSongAction,
            saveSongAsAction,
            loadSongAction,
            this::addLineAction
        );
        frame.setJMenuBar(menuBar);
    }

    // --- Dynamic UI Methods ---

    /**
     * Appends a new, blank SongLinePanel to the end of the composition and scrolls to it.
     */
    public void addLineAction() {
        SongLinePanel newPanel = new SongLinePanel();
        songLinePanels.add(newPanel);
        newPanel.setOnRemoveCallback(this::removeLinePanel);
        songLinePanelContainer.add(Box.createVerticalStrut(20));
        songLinePanelContainer.add(newPanel);
        
        // Auto-scroll to the newly added line to maintain user flow
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
        
        frame.revalidate();
        frame.repaint();
    }

    /**
     * Handles the visual removal of a line and notifies the Controller.
     * * @param panelToRemove The specific panel instance requested for deletion.
     */
    private void removeLinePanel(SongLinePanel panelToRemove) {
        int index = songLinePanels.indexOf(panelToRemove);
        if (index != -1) {
            removeLineCallback.accept(index);
            songLinePanels.remove(index);
            songLinePanelContainer.remove(panelToRemove);
            
            frame.revalidate();
            frame.repaint();
        }
    }

    /**
     * Completely rebuilds the UI based on a provided Song data model.
     * Used primarily when loading a file from disk.
     * * @param song The underlying data model containing lines, chords, and tunings.
     */
    public void refreshUIFromModel(Song song) {
        songLinePanels.clear();
        songLinePanelContainer.removeAll();
        
        songNameField.setText(song.getName());

        // Sync global tuning fields based on the first line of the song
        if (!song.getSongLines().isEmpty()) {
            SongLine firstLine = song.getSongLines().get(0);
            for (int i = 0; i < 6; i++) {
                String tuning = firstLine.getTablature().getGuitarStringTuning(i).trim();
                tuningFields[i].setText(tuning);
            }
        }

        // Recreate all panels to reflect the new song data
        for (SongLine songLine : song.getSongLines()) {
            SongLinePanel newPanel = new SongLinePanel();
            newPanel.getChordsField().setText(songLine.getChords());
            newPanel.getLyricsField().setText(songLine.getLyrics());
            newPanel.getTablatureArea().setText(songLine.getTablature().toString());
            
            newPanel.updateSongLine();
            newPanel.setOnRemoveCallback(this::removeLinePanel);
            
            songLinePanelContainer.add(newPanel);
            songLinePanelContainer.add(Box.createVerticalStrut(20));
            songLinePanels.add(newPanel);
        }
        
        frame.revalidate();
        frame.repaint();
    }

    /**
     * Clears all fields and resets the application to a blank, untitled state.
     */
    public void resetGUI() {
        songLinePanels.clear();
        songLinePanelContainer.removeAll();
        songNameField.setText("");
        
        for (int i = 0; i < 6; i++) {
            tuningFields[i].setText(" ");
        }
        
        addLineAction();
    }

    // --- State Exposure Getters for Controller ---

    public JFrame getFrame() { 
        return frame; 
    }
    
    public String getSongName() { 
        return songNameField.getText(); 
    }
    
    public List<SongLinePanel> getSongLinePanels() { 
        return songLinePanels; 
    }
    
    /**
     * Extracts the current tuning text from the header inputs.
     * * @return An array of strings representing the 6 guitar string tunings.
     */
    public String[] getTuningFieldsData() {
        String[] tunings = new String[6];
        for (int i = 0; i < 6; i++) {
            tunings[i] = tuningFields[i].getText();
        }
        return tunings;
    }
}