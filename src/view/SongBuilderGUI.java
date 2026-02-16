package view;

import controller.SongManager;
import model.Song;
import model.SongLine;
import model.Tablature;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DocumentFilter;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class SongBuilderGUI {
    private SongManager songManager;
    private JFrame frame;
    private JTextField songNameField;
    private JButton addLineButton, saveSongButton, loadSongButton;
    private ArrayList<SongLinePanel> songLinePanels;
    private JTextField[] tuningFields;
    private JScrollPane scrollPane;
    private JPanel songLinePanelContainer;

    public SongBuilderGUI() {
        songManager = new SongManager();
        songLinePanels = new ArrayList<>();
        tuningFields = new JTextField[6];
        Tablature tablature = new Tablature();

        // Initialize tuning fields with default values
        for (int i = 0; i < 6; i++) {
            String tuning = Character.toString(tablature.getGuitarStringTuning(i).charAt(0));
            tuningFields[i] = new JTextField(tuning, 2);
        }
        setupUI();
    }

    private void setupUI() {
        frame = new JFrame("SongBuilder");
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.setPreferredSize(new Dimension(850, 700));

        // --- Song Name Field ---
        songNameField = new JTextField(20);
        songNameField.setFont(new Font("Arial", Font.BOLD, 24));
        songNameField.setHorizontalAlignment(JTextField.CENTER);
        songNameField.setBackground(new Color(60, 60, 60));
        songNameField.setForeground(Color.WHITE);
        songNameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5) 
        ));
        Dimension SongNameFieldDim = new Dimension(400, 40); 
        songNameField.setPreferredSize(SongNameFieldDim);
        songNameField.setMaximumSize(SongNameFieldDim);

        // Menu Bar
        JMenuBar menuBar = createMenuBar();
        frame.setJMenuBar(menuBar);

        Font font = new Font("Arial", Font.PLAIN, 16);

        // Buttons
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
        
        // --- Tuning Section ---
        JLabel tuningLabel = new JLabel("Tuning:");
        tuningLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        frame.add(tuningLabel);
        
        JPanel tuningFieldsPanel = new JPanel(new FlowLayout());
        frame.add(tuningFieldsPanel);
        
        Dimension tuningFieldPanelDim = new Dimension(400, 45);
        
        tuningFieldsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 0, 0, 0),
            tuningFieldsPanel.getBorder()
        ));
        tuningFieldsPanel.setPreferredSize(tuningFieldPanelDim);
        tuningFieldsPanel.setMaximumSize(tuningFieldPanelDim);
        
        for (int i = tuningFields.length - 1; i >= 0; i--) {
            JTextField tuningField = tuningFields[i];
            Dimension tuningFieldsDim = new Dimension(35, 30);
            tuningField.setPreferredSize(tuningFieldsDim);
            tuningField.setHorizontalAlignment(JTextField.CENTER);
            tuningFieldsPanel.add(tuningField);
            ((AbstractDocument)tuningField.getDocument()).setDocumentFilter(new LengthFilter(2));
        }      
        
        // --- Song Line Container ---
        songLinePanelContainer = new JPanel();
        songLinePanelContainer.setLayout(new BoxLayout(songLinePanelContainer, BoxLayout.Y_AXIS));
        songLinePanelContainer.setBorder(new EmptyBorder(5, 5, 45, 5));

        scrollPane = new JScrollPane(songLinePanelContainer);     
        frame.add(scrollPane, BorderLayout.CENTER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        
        // Add Initial Panel
        SongLinePanel initialPanel = new SongLinePanel();
        songLinePanels.add(initialPanel);
        songLinePanelContainer.add(initialPanel);

        setupActionListeners();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(true); 
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        ActionMap actionMap = songNameField.getActionMap();
        
        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem newSongMenuItem = new JMenuItem("New File");
        JMenuItem saveSongMenuItem = new JMenuItem("Save");
        JMenuItem loadSongMenuItem = new JMenuItem("Load...");
        JMenuItem exitSongMenuItem = new JMenuItem("Exit");
        
        fileMenu.add(newSongMenuItem);
        fileMenu.add(saveSongMenuItem);
        fileMenu.add(loadSongMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitSongMenuItem);
        
        // Edit Menu
        JMenu editMenu = new JMenu("Edit");
        JMenuItem addLineMenuItem = new JMenuItem("Add Line...");
        JMenuItem removeLineMenuItem = new JMenuItem("Remove Line...");
        JMenuItem cutMenuItem = new JMenuItem(actionMap.get(DefaultEditorKit.cutAction));
        cutMenuItem.setText("Cut");
        JMenuItem copyMenuItem = new JMenuItem(actionMap.get(DefaultEditorKit.copyAction));
        copyMenuItem.setText("Copy");
        JMenuItem pasteMenuItem = new JMenuItem(actionMap.get(DefaultEditorKit.pasteAction));
        pasteMenuItem.setText("Paste");
        
        editMenu.add(addLineMenuItem);
        editMenu.add(removeLineMenuItem);
        editMenu.addSeparator();
        editMenu.add(cutMenuItem);
        editMenu.add(copyMenuItem);
        editMenu.add(pasteMenuItem);
        
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        
        // Exit Action
        exitSongMenuItem.addActionListener(e -> System.exit(0));
        
        // New Song Action
        newSongMenuItem.addActionListener(e -> resetGUI());

        addLineMenuItem.addActionListener(e -> addLineAction());
        saveSongMenuItem.addActionListener(e -> saveSongAction());
        loadSongMenuItem.addActionListener(e -> loadSongAction());

        // Remove Line Action
        removeLineMenuItem.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(frame, "Enter the number of the line to remove:");
            if (input != null && !input.isEmpty()) {
                try {
                    int index = Integer.parseInt(input);
                    if (index > 0 && index <= songLinePanels.size()) {
                        songManager.removeSongLine(index - 1);
                        SongLinePanel panelToRemove = songLinePanels.remove(index - 1);
                        songLinePanelContainer.remove(panelToRemove);
                        // Also remove any spacer components if they were added
                        frame.revalidate();
                        frame.repaint();
                    } else {
                        JOptionPane.showMessageDialog(frame, "Invalid line number.");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid index. Please enter a number.");
                }
            }
        });

        return menuBar;
    }

    private void setupActionListeners() {
        addLineButton.addActionListener(e -> addLineAction());
        saveSongButton.addActionListener(e -> saveSongAction());
        loadSongButton.addActionListener(e -> loadSongAction());
    }

    private void resetGUI() {
        songLinePanels.clear();
        songLinePanelContainer.removeAll();
        songNameField.setText("");
        SongLinePanel initialPanel = new SongLinePanel();
        songLinePanels.add(initialPanel);
        songLinePanelContainer.add(initialPanel);
        frame.revalidate();
        frame.repaint();
    }

    private void addLineAction() {
        SongLinePanel newPanel = new SongLinePanel();
        songLinePanels.add(newPanel);
        songLinePanelContainer.add(Box.createVerticalStrut(20)); // Smaller spacer for cleaner look
        songLinePanelContainer.add(newPanel);
        
        // Scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
        
        frame.revalidate();
        frame.repaint();
    }

    /**
     * Opens a file chooser dialog and loads a serialized JSON song model into the UI.
     */
    private void loadSongAction() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json"));
        int returnValue = fileChooser.showOpenDialog(frame);
        
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                songManager.loadSong(selectedFile); 
                Song song = songManager.getCurrentSong();
                
                // Update Name
                songNameField.setText(song.getName());
                
                // Update UI components
                refreshUIFromModel(song);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error loading file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    /**
     * Synchronizes the UI state with the data model and persists it to a JSON file.
     */
    private void saveSongAction() {
        // 1. Gather Tunings
        String[] tunings = new String[6];
        for (int i = 0; i < 6; i++) {
            tunings[i] = tuningFields[i].getText();
        }

        // 2. Clear current song model lines
        songManager.getCurrentSong().getSongLines().clear();

        // 3. Re-populate model from UI Panels
        for (SongLinePanel panel : songLinePanels) {
            panel.updateSongLine();
            SongLine line = panel.getSongLine();
            
            // Apply global tunings to this line's tablature
            for (int i = 0; i < 6; i++) {
                String t = tunings[i];
                if (t.length() < 2) t = String.format("%-2s", t);
                line.getTablature().setGuitarStringTuning(i, t);
            }
            
            songManager.getCurrentSong().addSongLine(line);
        }

        // 4. Update Song Name
        String name = songNameField.getText();
        if (name.trim().isEmpty()) name = "Untitled";
        songManager.getCurrentSong().setName(name);

        // 5. Save
        try {
            if (songManager.getCurrentFile() == null) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save Song As...");
                fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json"));
                fileChooser.setSelectedFile(new File(name + ".json"));
                
                int returnValue = fileChooser.showSaveDialog(frame);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    songManager.saveSong(fileChooser.getSelectedFile());
                    JOptionPane.showMessageDialog(frame, "Song saved successfully!");
                }
            } else {
                songManager.saveSong(songManager.getCurrentFile());
                // We perform a silent save here to keep the musician's workflow uninterrupted
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Error saving file: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    /**
     * Rebuilds the UI based on a Song object.
     */
    private void refreshUIFromModel(Song song) {
        songLinePanels.clear();
        songLinePanelContainer.removeAll();
        
        // Handle Tuning from first line (if exists)
        if (!song.getSongLines().isEmpty()) {
            SongLine firstLine = song.getSongLines().get(0);
            for (int i = 0; i < 6; i++) {
                String tuning = firstLine.getTablature().getGuitarStringTuning(i).trim();
                tuningFields[i].setText(tuning);
            }
        }

        // Recreate Panels
        for (SongLine songLine : song.getSongLines()) {
            SongLinePanel newPanel = new SongLinePanel();
            
            newPanel.getChordsField().setText(songLine.getChords());
            newPanel.getLyricsField().setText(songLine.getLyrics());
            newPanel.getTablatureArea().setText(songLine.getTablature().toString());
            
            newPanel.updateSongLine(); 
            
            songLinePanelContainer.add(newPanel);
            songLinePanelContainer.add(Box.createVerticalStrut(20)); // Consistent spacing
            songLinePanels.add(newPanel);
        }
        
        frame.revalidate();
        frame.repaint();
    }
}

// Inner class for filtering text length
class LengthFilter extends DocumentFilter {
    private int max;
    LengthFilter(int max) { this.max = max; }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (this.max < 0 || fb.getDocument().getLength() + string.length() <= this.max) {
            super.insertString(fb, offset, string, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if (this.max < 0 || fb.getDocument().getLength() + text.length() - length <= this.max) {
            super.replace(fb, offset, length, text, attrs);
        }
    }
}