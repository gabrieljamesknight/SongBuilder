package view.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DefaultEditorKit;

import model.SongLine;
import model.Tablature;
import view.listeners.ChordsInputHandler;
import view.listeners.LyricsInputHandler;
import view.listeners.TablatureInputHandler;

/**
 * Represents a single musical line within the application's UI, integrating chords, 
 * tablature, and lyrics.
 * * Acts as a composite view component, delegating specific input handling and 
 * formatting rules to dedicated listener classes to maintain a clean architecture.
 */
public class SongLinePanel extends JPanel {
    private JTextField chordsField, lyricsField, sectionLabelField;
    private JLabel lineNumberLabel;
    private JLabel dragHandleLabel;
    private JTextArea tablatureArea;
    private final SongLine songLine;
    protected boolean isProgrammaticUpdate = false;
    private Consumer<SongLinePanel> onRemoveCallback;
    private ChordsInputHandler chordsHandler;
    private TablatureInputHandler tablatureHandler;
    private LyricsInputHandler lyricsHandler;

    /**
     * Constructs a new SongLinePanel, initializing the UI components and their respective layout constraints.
     */
    public SongLinePanel() {
        super();
        this.setLayout(new GridBagLayout());
        this.songLine = new SongLine();
        Font lyricsFont = new Font("Monospaced", Font.PLAIN, 16);

        this.setMaximumSize(new Dimension(850, 240));

        // --- Initialize Modular Fields ---
        initHeaderComponents();
        initChordsField();
        initTablatureArea();
        initLyricsField(lyricsFont);

        JButton removeButton = new JButton("🗑");
        removeButton.setToolTipText("Remove this line");
        removeButton.setFocusable(false); 
        removeButton.addActionListener(e -> {
            if (this.onRemoveCallback != null) {
                this.onRemoveCallback.accept(this);
            }
        });

        layoutComponents(removeButton);
    }

    private void initHeaderComponents() {
        lineNumberLabel = new JLabel("# ");
        lineNumberLabel.setFont(new Font("Arial", Font.BOLD, 14));
        lineNumberLabel.setForeground(new Color(150, 150, 150));

        sectionLabelField = new JTextField();
        sectionLabelField.setFont(new Font("Arial", Font.ITALIC | Font.BOLD, 14));
        sectionLabelField.setForeground(new Color(200, 200, 200));
        sectionLabelField.setOpaque(false); // Makes the background transparent
        sectionLabelField.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0)); // Removes the border
        
        // Add a document filter to prevent excessively long labels breaking the UI
        ((AbstractDocument) sectionLabelField.getDocument()).setDocumentFilter(new LengthFilter(30));
    }

    /**
     * Initializes the chords input field and attaches its specific input handler.
     */
    private void initChordsField() {
        chordsField = new JTextField();
        chordsField.setFont(new Font("Monospaced", Font.PLAIN, 20));
        chordsField.setText(" ".repeat(47));
        chordsField.setHorizontalAlignment(JTextField.LEFT);
        
        LengthFilter lengthFilter = new LengthFilter(50);
        ((AbstractDocument) chordsField.getDocument()).setDocumentFilter(lengthFilter);
        
        chordsField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 2),
            new EmptyBorder(0, 30, 0, 0)
        ));

        this.chordsHandler = new ChordsInputHandler(chordsField);
    }

    /**
     * Initializes the tablature text area, establishes baseline empty strings, 
     * and attaches its specialized input handler.
     */
    private void initTablatureArea() {
        tablatureArea = new JTextArea() {
            @Override
            protected void processKeyEvent(KeyEvent ke) {
                if ((ke.getKeyCode() == KeyEvent.VK_C && ke.isControlDown()) || 
                    (ke.getKeyCode() == KeyEvent.VK_V && ke.isControlDown())) {
                    ke.consume();
                } else {
                    super.processKeyEvent(ke);
                }
            }
        };
        tablatureArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        tablatureArea.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        tablatureArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), DefaultEditorKit.forwardAction);
        tablatureArea.getInputMap().put(KeyStroke.getKeyStroke(' '), DefaultEditorKit.forwardAction);
        
        Tablature emptyTablature = new Tablature();
        tablatureArea.setText(emptyTablature.toString());

        this.tablatureHandler = new TablatureInputHandler(tablatureArea);
    }

    /**
     * Initializes the lyrics text field and delegates its configuration and constraints 
     * to the LyricsInputHandler.
     * * @param font The font to apply to the lyrics text field.
     */
    private void initLyricsField(Font font) {
        lyricsField = new JTextField();
        this.lyricsHandler = new LyricsInputHandler(lyricsField, font);
    }

    /**
     * Arranges the initialized components within the panel using GridBagLayout.
     * * @param removeButton The button used to trigger the removal of this panel.
     */
    private void layoutComponents(JButton removeButton) {
        int targetWidth = 600;
        
        // 1. Header (Line Number & Section Label)
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
        headerPanel.setOpaque(false);
        // Add 30px left padding so the label perfectly aligns with the chords text
        headerPanel.setBorder(new EmptyBorder(0, 30, 0, 0)); 
        headerPanel.setPreferredSize(new Dimension(targetWidth, 25));
        headerPanel.setMaximumSize(new Dimension(targetWidth, 25));
        
        headerPanel.add(lineNumberLabel);
        headerPanel.add(sectionLabelField);

        // 2. Component Dimensions
        Dimension chordsDim = new Dimension(targetWidth, 35);
        Dimension tabDim    = new Dimension(targetWidth, 100);
        Dimension lyricsDim = new Dimension(targetWidth, 26);
        
        chordsField.setPreferredSize(chordsDim);
        chordsField.setMaximumSize(chordsDim);
        
        tablatureArea.setPreferredSize(tabDim);
        tablatureArea.setMaximumSize(tabDim);

        lyricsField.setPreferredSize(lyricsDim);
        lyricsField.setMaximumSize(lyricsDim);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        
        // --- ROW 0: Header ---
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 2, 0); 
        gbc.anchor = GridBagConstraints.WEST;
        this.add(headerPanel, gbc);

        // --- ROW 1: Chords ---
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 5, 0); 
        gbc.anchor = GridBagConstraints.CENTER;
        this.add(chordsField, gbc);

        // --- ROW 2: Tablature & Remove Button ---
        gbc.gridx = 1; gbc.gridy = 2; 
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.anchor = GridBagConstraints.CENTER; 
        this.add(tablatureArea, gbc);

        gbc.gridx = 2; gbc.gridy = 2;
        gbc.insets = new Insets(0, 15, 5, 0); 
        gbc.anchor = GridBagConstraints.WEST;
        this.add(removeButton, gbc);

        // --- ROW 3: Lyrics ---
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 15, 0); 
        gbc.anchor = GridBagConstraints.CENTER;
        this.add(lyricsField, gbc);

        // --- ROW 4: Drag Handle ---
        dragHandleLabel = new JLabel("≡");
        dragHandleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        dragHandleLabel.setForeground(new Color(100, 100, 100));
        dragHandleLabel.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        dragHandleLabel.setToolTipText("Click and drag to reorder this line");

        gbc.gridx = 0; 
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 20, 5, 15);
        gbc.anchor = GridBagConstraints.EAST;
        this.add(dragHandleLabel, gbc);
    }

    /**
     * Visually updates the tuning of a specific string in the tablature area without 
     * triggering document listener loops.
     * * @param stringIndex The 0-based index of the guitar string.
     * @param newTuning   The new tuning character(s) to apply.
     */
    public void updateTuningVisually(int stringIndex, String newTuning) {
        String formattedTuning = String.format("%-2s", newTuning);
        String currentText = tablatureArea.getText();
        String[] lines = currentText.split("\n");
        
        if (stringIndex >= 0 && stringIndex < lines.length) {
            if (lines[stringIndex].length() >= 2) {
                lines[stringIndex] = formattedTuning + lines[stringIndex].substring(2);
                SwingUtilities.invokeLater(() -> {
                    isProgrammaticUpdate = true;
                    try {
                        tablatureArea.setText(String.join("\n", lines));
                    } finally {
                        isProgrammaticUpdate = false;
                    }
                });
            }
        }
    }

    public void setLineNumber(int number) {
        lineNumberLabel.setText(String.valueOf(number) + ". ");
    }

    /**
     * Sets the callback to be executed when the remove button is clicked.
     * * @param callback The consumer function to handle panel removal.
     */
    public void setOnRemoveCallback(Consumer<SongLinePanel> callback) {
        this.onRemoveCallback = callback;
    }

    /**
     * Commits the current UI field values into the underlying SongLine model object.
     */
    public void updateSongLine() {
        songLine.setChords(getChords());
        songLine.setLyrics(getLyrics());
        songLine.setTablature(getTablature());
        songLine.setSectionLabel(sectionLabelField.getText().trim());
    }


    public JLabel getDragHandle() { return dragHandleLabel; }
    public JTextField getSectionLabelField() { return sectionLabelField; }
    public SongLine getSongLine() { return songLine; }
    public String getChords() { return chordsField.getText(); }
    public String getLyrics() { return lyricsField.getText(); }
    public Tablature getTablature() { return Tablature.parseTablature(tablatureArea.getText()); }
    
    public JTextField getChordsField() { return chordsField; }
    public JTextField getLyricsField() { return lyricsField; }
    public JTextArea getTablatureArea() { return tablatureArea; }

    public ChordsInputHandler getChordsHandler() { return chordsHandler; }
    public TablatureInputHandler getTablatureHandler() { return tablatureHandler; }
    public LyricsInputHandler getLyricsHandler() { return lyricsHandler; }
}