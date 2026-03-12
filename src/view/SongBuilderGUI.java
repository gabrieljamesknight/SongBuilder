package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import model.Song;
import model.SongLine;
import view.components.SongBuilderMenuBar;
import view.components.SongHeaderPanel;
import view.components.SongLinePanel;
import view.components.TuningPanel;

/**
 * The main graphical user interface for the SongBuilder application.
 *
 * Operates strictly as a View component within the MVC architecture.
 * Handles high-level layout orchestration, routing data to the Controller,
 * and managing the scrollable tablature grid.
 */
public class SongBuilderGUI {
    
    private JFrame frame;
    private SongHeaderPanel headerPanel;
    private final ArrayList<SongLinePanel> songLinePanels;
    private TuningPanel tuningPanel;
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

        // Initialize the modular tuning panel with default standard tuning
        String[] defaultTunings = {"e", "B", "G", "D", "A", "E"};
        tuningPanel = new TuningPanel(defaultTunings, this::handleGlobalTuningChange);
        
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

        // Initialize the new Header Panel with lazily evaluated callbacks
        headerPanel = new SongHeaderPanel(
            this::addLineAction, 
            () -> saveSongAction.run(), 
            () -> loadSongAction.run()
        );

        frame.add(Box.createRigidArea(new Dimension(0, 10)));
        frame.add(headerPanel);
        frame.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Add the modular tuning panel
        frame.add(tuningPanel);
        frame.add(Box.createRigidArea(new Dimension(0, 10)));

        // Song Line Container (Scrollable area for tablature)
        songLinePanelContainer = new JPanel();
        songLinePanelContainer.setLayout(new BoxLayout(songLinePanelContainer, BoxLayout.Y_AXIS));
        songLinePanelContainer.setBorder(new EmptyBorder(5, 5, 45, 5));

        scrollPane = new JScrollPane(songLinePanelContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);    
        frame.add(scrollPane, BorderLayout.CENTER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);

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
            headerPanel.getTextFieldActionMap(), // Delegate to the extracted component
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
        newPanel.setOnRemoveCallback(this::removeLinePanel);
        
        // Only add spacing if this isn't the first panel being added to the container
        if (!songLinePanels.isEmpty()) {
            songLinePanelContainer.add(Box.createVerticalStrut(20));
        }
        
        songLinePanels.add(newPanel);
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
     *
     * @param panelToRemove The specific panel instance requested for deletion.
     */
    private void removeLinePanel(SongLinePanel panelToRemove) {
        int index = songLinePanels.indexOf(panelToRemove);
        if (index != -1) {
            // Notify the controller
            removeLineCallback.accept(index);
            
            // Remove from the UI tracking list
            songLinePanels.remove(index);
            
            // Rebuild the container to clear orphaned vertical struts
            songLinePanelContainer.removeAll();
            for (int i = 0; i < songLinePanels.size(); i++) {
                if (i > 0) {
                    songLinePanelContainer.add(Box.createVerticalStrut(20));
                }
                songLinePanelContainer.add(songLinePanels.get(i));
            }
            
            frame.revalidate();
            frame.repaint();
        }
    }

    /**
     * Completely rebuilds the UI based on a provided Song data model.
     * Used primarily when loading a file from disk.
     *
     * @param song The underlying data model containing lines, chords, and tunings.
     */
    public void refreshUIFromModel(Song song) {
        songLinePanels.clear();
        songLinePanelContainer.removeAll();
        
        headerPanel.setSongName(song.getName());
        
        // Sync global tuning fields based on the first line of the song
        if (!song.getSongLines().isEmpty()) {
            SongLine firstLine = song.getSongLines().get(0);
            for (int i = 0; i < 6; i++) {
                String tuning = firstLine.getTablature().getGuitarStringTuning(i).trim();
                tuningPanel.setTuningSilently(i, tuning);
            }
        }

        // Recreate all panels to reflect the new song data
        for (int i = 0; i < song.getSongLines().size(); i++) {
            SongLine songLine = song.getSongLines().get(i);
            SongLinePanel newPanel = new SongLinePanel();
            
            newPanel.getChordsField().setText(songLine.getChords());
            newPanel.getLyricsField().setText(songLine.getLyrics());
            newPanel.getTablatureArea().setText(songLine.getTablature().toString());
            
            newPanel.updateSongLine();
            newPanel.setOnRemoveCallback(this::removeLinePanel);
            
            // Only add spacing before the panel, matching addLineAction's logic
            if (i > 0) {
                songLinePanelContainer.add(Box.createVerticalStrut(20));
            }
            
            songLinePanelContainer.add(newPanel);
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
        headerPanel.setSongName("");
        
        String[] defaultTunings = {"e", "B", "G", "D", "A", "E"};
        tuningPanel = new TuningPanel(defaultTunings, this::handleGlobalTuningChange);
        for (int i = 0; i < 6; i++) {
            tuningPanel.setTuningSilently(i, defaultTunings[i]);
        }
        
        addLineAction();
    }
    
    /**
     * Callback triggered when a user changes a tuning in the TuningPanel.
     * Visually cascades the tuning change to all active SongLinePanels.
     *
     * @param stringIndex The 0-based index of the guitar string.
     * @param newTuning   The updated tuning string.
     */
    private void handleGlobalTuningChange(int stringIndex, String newTuning) {
        if (songLinePanels == null) return;
        for (SongLinePanel panel : songLinePanels) {
            panel.updateTuningVisually(stringIndex, newTuning);
        }
    }

    // --- State Exposure Getters for Controller ---

    public JFrame getFrame() { 
        return frame;
    }
    
    public String getSongName() { 
        return headerPanel.getSongName();
    }
    
    public List<SongLinePanel> getSongLinePanels() { 
        return songLinePanels;
    }
    
    /**
     * Extracts the current tuning text from the header inputs.
     *
     * @return An array of strings representing the 6 guitar string tunings.
     */
    public String[] getTuningFieldsData() {
        return tuningPanel.getCurrentTunings();
    }
}