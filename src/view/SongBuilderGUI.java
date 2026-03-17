package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import view.listeners.SongLineActionObserver;
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
import view.listeners.PanelDragDropHandler;

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
    private PanelDragDropHandler dragDropHandler;
    private Runnable newSongAction = () -> {};
    private Runnable saveSongAction = () -> {};
    private Runnable saveSongAsAction = () -> {};
    private Runnable loadSongAction = () -> {};
    private Consumer<Integer> removeLineCallback = (index) -> {};
    private SongLine clipboardLine = null;
    private SongLinePanel lastFocusedPanel = null;
    private final SongLineActionObserver panelActionObserver = new SongLineActionObserver() {
        @Override
        public void onRemove(SongLinePanel panel) { removeLinePanel(panel); }
        @Override
        public void onCopy(SongLinePanel panel) { copyLinePanel(panel); }
        @Override
        public void onPasteBelow(SongLinePanel panel) { pasteLineBelowPanel(panel); }
        @Override
        public void onDuplicate(SongLinePanel panel) { duplicateLinePanel(panel); }
        @Override
        public void onFocus(SongLinePanel panel) { setActivePanel(panel); }
    };

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
        frame.setPreferredSize(new Dimension(800, 700));

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
        songLinePanelContainer.setBorder(new EmptyBorder(15, 10, 45, 10));
        songLinePanelContainer.setBackground(new java.awt.Color(25, 27, 30));
        
        // Ensure the scroll pane viewport matches the container's deep background
        scrollPane = new JScrollPane(songLinePanelContainer);
        scrollPane.getViewport().setBackground(new java.awt.Color(25, 27, 30));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);    
        frame.add(scrollPane, BorderLayout.CENTER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        
        // Initialize the drag-and-drop handler
        dragDropHandler = new PanelDragDropHandler(
            songLinePanelContainer, 
            songLinePanels, 
            this::updateAllLineNumbers
        );
        updateMenuBar();

        // Attach global focus clearing for improved UX
        setupGlobalFocusClearing();

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
            this::addLineAction,
            // Wrap the specific panel operations so they resolve the active panel at click-time
            () -> { SongLinePanel p = getTargetPanel(); if (p != null) copyLinePanel(p); },
            () -> { SongLinePanel p = getTargetPanel(); if (p != null) pasteLineBelowPanel(p); },
            () -> { SongLinePanel p = getTargetPanel(); if (p != null) duplicateLinePanel(p); },
            () -> { SongLinePanel p = getTargetPanel(); if (p != null) p.clearContents(); },
            () -> { SongLinePanel p = getTargetPanel(); if (p != null) removeLinePanel(p); }
        );
        frame.setJMenuBar(menuBar);
    }

    // --- Dynamic UI Methods ---

/**
     * Appends a new, blank SongLinePanel to the end of the composition and scrolls to it.
     */
    public void addLineAction() {
        SongLinePanel newPanel = new SongLinePanel();
        newPanel.setLineNumber(songLinePanels.size() + 1);
        
        // Link all context menu and button callbacks here
        newPanel.setActionObserver(panelActionObserver);
        
        if (!songLinePanels.isEmpty()) {
            songLinePanelContainer.add(Box.createVerticalStrut(20));
        }

        // Attach drag and drop listeners
        newPanel.getDragHandle().addMouseListener(dragDropHandler);
        newPanel.getDragHandle().addMouseMotionListener(dragDropHandler);
        
        // CRITICAL: These are the lines that actually put the panel on the screen
        songLinePanels.add(newPanel);
        songLinePanelContainer.add(newPanel);
        
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
        if (this.lastFocusedPanel == panelToRemove) {
            setActivePanel(null);
        }
        
        int index = songLinePanels.indexOf(panelToRemove);

        if (index != -1) {
            removeLineCallback.accept(index);
            songLinePanels.remove(index);
            songLinePanelContainer.removeAll();

            // Rebuild the container and update line numbers to prevent skipping
            for (int i = 0; i < songLinePanels.size(); i++) {
                SongLinePanel panel = songLinePanels.get(i);
                panel.setLineNumber(i + 1); // Recalculate 1-based index
                
                if (i > 0) {
                    songLinePanelContainer.add(Box.createVerticalStrut(20));
                }
                songLinePanelContainer.add(panel);
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
        if (!song.getSongLines().isEmpty()) {
            SongLine firstLine = song.getSongLines().get(0);
            for (int i = 0; i < 6; i++) {
                String tuning = firstLine.getTablature().getGuitarStringTuning(i).trim();
                tuningPanel.setTuningSilently(i, tuning);
            }
        }

        for (int i = 0; i < song.getSongLines().size(); i++) {
            SongLine songLine = song.getSongLines().get(i);
            SongLinePanel newPanel = new SongLinePanel();
            
            newPanel.setLineNumber(i + 1); 
            
            newPanel.getChordsField().setText(songLine.getChords());
            newPanel.getLyricsField().setText(songLine.getLyrics());
            newPanel.getTablatureArea().setText(songLine.getTablature().toString());
            
            if (songLine.getSectionLabel() != null) {
                newPanel.getSectionLabelField().setText(songLine.getSectionLabel());
            }
            
            newPanel.updateSongLine();
            
            // Link all context menu and button callbacks here
            newPanel.setActionObserver(panelActionObserver);

            if (i > 0) {
                songLinePanelContainer.add(Box.createVerticalStrut(20));
            }

            // Attach drag and drop listeners
            newPanel.getDragHandle().addMouseListener(dragDropHandler);
            newPanel.getDragHandle().addMouseMotionListener(dragDropHandler);
            
            // CRITICAL: Put the panel on the screen
            songLinePanelContainer.add(newPanel);
            songLinePanels.add(newPanel);
        }
        
        frame.revalidate();
        frame.repaint();
    }

    /**
     * Recalculates and updates the visual line numbers for all active panels.
     * Triggered automatically after a drag-and-drop reorder.
     */
    private void updateAllLineNumbers() {
        for (int i = 0; i < songLinePanels.size(); i++) {
            songLinePanels.get(i).setLineNumber(i + 1);
        }
    }

/**
     * Deep copies the content of the selected panel into the internal clipboard.
     *
     * @param panel The source panel being copied.
     */
    private void copyLinePanel(SongLinePanel panel) {
        panel.updateSongLine();
        SongLine source = panel.getSongLine();
        
        // Deep copy the state using the toString/parse trick for Tablature to avoid reference mutations
        this.clipboardLine = new SongLine(
            source.getChords(), 
            source.getLyrics(), 
            model.Tablature.parseTablature(source.getTablature().toString())
        );
        this.clipboardLine.setSectionLabel(source.getSectionLabel());
        
        System.out.println("Copied line to clipboard!");
    }

    /**
     * Injects a newly constructed panel containing the clipboard data directly below the target panel.
     *
     * @param targetPanel The panel that triggered the paste action.
     */
    private void pasteLineBelowPanel(SongLinePanel targetPanel) {
        if (this.clipboardLine == null) {
            System.out.println("Cannot paste: Clipboard is empty.");
            return; 
        }
        
        int targetIndex = songLinePanels.indexOf(targetPanel);
        if (targetIndex == -1) return;

        SongLinePanel newPanel = new SongLinePanel();
        
        // Populate the new panel with clipboard data
        newPanel.getChordsField().setText(this.clipboardLine.getChords());
        newPanel.getLyricsField().setText(this.clipboardLine.getLyrics());
        newPanel.getTablatureArea().setText(this.clipboardLine.getTablature().toString());
        newPanel.getSectionLabelField().setText(this.clipboardLine.getSectionLabel() != null ? this.clipboardLine.getSectionLabel() : "");
        newPanel.updateSongLine();

        // Attach callbacks to the cloned panel so the new panel can also be copied/pasted
        newPanel.setActionObserver(panelActionObserver);
        newPanel.getDragHandle().addMouseListener(dragDropHandler);
        newPanel.getDragHandle().addMouseMotionListener(dragDropHandler);

        // Insert into the underlying list
        songLinePanels.add(targetIndex + 1, newPanel);
        
        // Rebuild the container to visually insert the panel in the correct vertical slot
        songLinePanelContainer.removeAll();
        for (int i = 0; i < songLinePanels.size(); i++) {
            songLinePanels.get(i).setLineNumber(i + 1);
            if (i > 0) {
                songLinePanelContainer.add(Box.createVerticalStrut(20));
            }
            songLinePanelContainer.add(songLinePanels.get(i));
        }
        
        frame.revalidate();
        frame.repaint();
        
        System.out.println("Pasted line successfully at index " + (targetIndex + 1));
    }

    /**
     * Attaches mouse listeners to background containers to steal component focus 
     * when the user clicks on an empty area of the application.
     */
    private void setupGlobalFocusClearing() {
        // 1. Make the background container capable of receiving focus 
        // (JPanels are not focusable by default)
        songLinePanelContainer.setFocusable(true);
        
        java.awt.event.MouseAdapter clearFocusAdapter = new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                // 2. Explicitly transfer focus to the background container.
                // This guarantees the active focusOwner is NO LONGER a descendant 
                // of the SongLinePanel, allowing updatePanelBorder() to evaluate to false.
                songLinePanelContainer.requestFocusInWindow();

                setActivePanel(null);
            }
        };

        // Attach to the main frame and background containers
        frame.addMouseListener(clearFocusAdapter);
        songLinePanelContainer.addMouseListener(clearFocusAdapter);
        
        // The viewport is the actual background of the scrollable area
        scrollPane.getViewport().addMouseListener(clearFocusAdapter);
        
        // Also attach to the header panel and tuning panel
        headerPanel.addMouseListener(clearFocusAdapter);
        tuningPanel.addMouseListener(clearFocusAdapter);
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
     * Sets the globally active panel, managing the sticky highlight state 
     * so panels don't lose their blue border when menus are opened.
     */
    private void setActivePanel(SongLinePanel panel) {
        if (this.lastFocusedPanel != null && this.lastFocusedPanel != panel) {
            this.lastFocusedPanel.setForceHighlight(false); // Turn off old highlight
        }
        
        this.lastFocusedPanel = panel;
        
        if (this.lastFocusedPanel != null) {
            this.lastFocusedPanel.setForceHighlight(true); // Turn on new highlight
        }
    }

    /**
     * Resolves the target panel for global menu actions.
     * Defaults to the currently focused panel, or the last panel in the composition if none are focused.
     */
    private SongLinePanel getTargetPanel() {
        if (lastFocusedPanel != null && songLinePanels.contains(lastFocusedPanel)) {
            return lastFocusedPanel;
        }
        if (!songLinePanels.isEmpty()) {
            return songLinePanels.get(songLinePanels.size() - 1);
        }
        return null;
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

    /**
     * Duplicates the target panel directly below it by firing a copy and paste sequentially.
     * * @param targetPanel The panel to duplicate.
     */
    private void duplicateLinePanel(SongLinePanel targetPanel) {
        // We can reuse the exact logic we just built!
        copyLinePanel(targetPanel);
        pasteLineBelowPanel(targetPanel);
        System.out.println("Line duplicated successfully.");
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