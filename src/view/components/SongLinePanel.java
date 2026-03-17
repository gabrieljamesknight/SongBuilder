package view.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import controller.SongLineMapper;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import model.SongLine;
import model.Tablature;
import view.listeners.ChordsInputHandler;
import view.listeners.LyricsInputHandler;
import view.listeners.SongLineActionObserver;
import view.listeners.TablatureInputHandler;

/**
 * Represents a single musical line within the application's UI, integrating chords, 
 * tablature, and lyrics.
 * * Acts as a composite view component, delegating specific input handling and 
 * formatting rules to dedicated listener classes to maintain a clean architecture.
 */
public class SongLinePanel extends JPanel {
    private JTextField chordsField, lyricsField, sectionLabelField;
    private JLabel dragHandleLabel;
    private TablatureTextArea tablatureArea;
    private final SongLine songLine;
    private FocusAwareBorderPanel innerContentPanel;
    private SongLineActionObserver actionObserver;
    private ChordsInputHandler chordsHandler;
    private TablatureInputHandler tablatureHandler;
    private LyricsInputHandler lyricsHandler;
    private SongLineDragVisualizer dragVisualizer;
    

    /**
     * Constructs a new SongLinePanel, initializing the UI components and their respective layout constraints.
     */
    public SongLinePanel() {
        super();
        this.setLayout(new java.awt.BorderLayout(0, 5));
        this.setOpaque(false);
        
        this.songLine = new SongLine();
        Font lyricsFont = new Font("Monospaced", Font.PLAIN, 16);

        // Slightly taller to accommodate the external section label
        this.setMaximumSize(new Dimension(850, 280)); 

        // Initialize the inner panel that will hold the actual song data and receive the border
        this.innerContentPanel = new FocusAwareBorderPanel(SongLineComponentFactory.createDefaultBorder(), SongLineComponentFactory.createFocusedBorder());
        this.innerContentPanel.setLayout(new GridBagLayout());
        this.innerContentPanel.setBackground(new java.awt.Color(45, 48, 52));
        this.innerContentPanel.setOpaque(false);

        this.innerContentPanel.setOnFocusGainedAction(() -> {
            if (actionObserver != null) {
                actionObserver.onFocus(this);
            }
        });

        this.setBackground(new java.awt.Color(45, 48, 52));
        this.setOpaque(false);


        // --- Initialize Modular Fields ---
        initHeaderComponents();
        initChordsField();
        initTablatureArea();
        initLyricsField(lyricsFont);

        this.innerContentPanel.attachFocusTracking(this.innerContentPanel);

        HoverButton removeButton = SongLineComponentFactory.createRemoveButton(() -> {
            if (this.actionObserver != null) {
                this.actionObserver.onRemove(this);
            }
        });

        initContextMenu();
        layoutComponents(removeButton);

        this.innerContentPanel.attachFocusTracking(this.innerContentPanel);
        this.dragVisualizer = new SongLineDragVisualizer(this, innerContentPanel, dragHandleLabel, sectionLabelField);
    }

    private void initHeaderComponents() {
        sectionLabelField = new JTextField();
        // Make it prominent as a structural divider
        sectionLabelField.setFont(new Font("Arial", Font.BOLD, 18));
        sectionLabelField.setForeground(new Color(120, 170, 220)); 
        sectionLabelField.setOpaque(false);
        sectionLabelField.setHorizontalAlignment(JTextField.CENTER);
        sectionLabelField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        ((AbstractDocument) sectionLabelField.getDocument()).setDocumentFilter(new LengthFilter(30));
    }

    /**
     * Initializes the chords input field and attaches its specific input handler.
     */
    private void initChordsField() {
        chordsField = new JTextField();
        chordsField.setFont(new Font("Monospaced", Font.PLAIN, 19));
        chordsField.setText(" ".repeat(50));
        chordsField.setHorizontalAlignment(JTextField.LEFT);
        
        LengthFilter lengthFilter = new LengthFilter(60);
        ((AbstractDocument) chordsField.getDocument()).setDocumentFilter(lengthFilter);
        
        // Use the native rounded border and apply necessary padding for text alignment
        chordsField.setBorder(BorderFactory.createCompoundBorder(
            javax.swing.UIManager.getBorder("TextField.border"),
            new EmptyBorder(4, 30, 4, 4)
        ));
        this.chordsHandler = new ChordsInputHandler(chordsField);
    }

    /**
     * Initializes the tablature text area using the extracted TablatureTextArea component,
     * and attaches its specialized input handler to maintain the structural grid.
     */
    private void initTablatureArea() {
        tablatureArea = new TablatureTextArea();
        this.tablatureHandler = new TablatureInputHandler(tablatureArea);
        this.innerContentPanel.attachFocusTracking(this.innerContentPanel);
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
    
    private void initContextMenu() {
        SongLineContextMenu menu = new SongLineContextMenu(this, this::clearContents);
        menu.attachTo(this, innerContentPanel);
    }

    public void setForceHighlight(boolean force) {
        this.innerContentPanel.setForceHighlight(force);
    }

    /**
     * Commits the current UI field values into the underlying SongLine model object.
     */
    public void updateSongLine() {
        SongLineMapper.updateModelFromUI(
            this.songLine, 
            this.chordsField, 
            this.lyricsField, 
            this.tablatureArea, 
            this.sectionLabelField
        );
    }

    /**
     * Resets the textual content of the panel to an empty, formatted state.
     */
    public void clearContents() {
        SongLineMapper.clearUIContents(
            this.chordsField, 
            this.lyricsField, 
            this.tablatureArea, 
            this.sectionLabelField
        );
        updateSongLine();
    }


    /**
     * Arranges the initialized components to establish a balanced, professional musical grid.
     * Integrates the line number and drag handle to share a horizontal axis left of the tablature.
     *
     * @param removeButton The button used to trigger the removal of this panel.
     */
    private void layoutComponents(JButton removeButton) {
        int targetWidth = 600;

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

        // 3. Drag Handle Setup (Inside the Box)
        dragHandleLabel = SongLineComponentFactory.createDragHandle();
        
        
        // --- Left Margin Panel (Contains ONLY the Line Number) ---
        JPanel leftMarginPanel = new JPanel(new GridBagLayout());
        leftMarginPanel.setOpaque(false);
        GridBagConstraints marginGbc = new GridBagConstraints();
        marginGbc.insets = new Insets(4, 0, 0, 15);
        marginGbc.anchor = GridBagConstraints.CENTER;
        
        // COLUMN 0: Line Number andDrag Handle
        innerContentPanel.add(dragHandleLabel, GridBagHelper.createConstraints(0, 1, 1.0, new Insets(0, 0, 5, 10), GridBagConstraints.CENTER));
        
        // COLUMN 1: Chords, Tablature, and Lyrics (weightx = 0.0 to maintain preferred width)
        innerContentPanel.add(chordsField, GridBagHelper.createConstraints(1, 0, 0.0, new Insets(10, 0, 5, 0), GridBagConstraints.WEST));
        innerContentPanel.add(tablatureArea, GridBagHelper.createConstraints(1, 1, 0.0, new Insets(5, 0, 5, 0), GridBagConstraints.WEST));
        innerContentPanel.add(lyricsField, GridBagHelper.createConstraints(1, 2, 0.0, new Insets(5, 0, 10, 0), GridBagConstraints.WEST));
        
        // COLUMN 2: Remove Button
        innerContentPanel.add(removeButton, GridBagHelper.createConstraints(2, 1, 1.0, new Insets(0, 8, 7, 0), GridBagConstraints.CENTER));
        
        // Assemble the outer panel
        this.add(sectionLabelField, java.awt.BorderLayout.NORTH);
        this.add(innerContentPanel, java.awt.BorderLayout.CENTER);
    }

    /**
     * Delegates the visual tuning update to the tablature handler to safely bypass listeners.
     */
    public void updateTuningVisually(int stringIndex, String newTuning) {
        if (tablatureHandler != null) {
            tablatureHandler.updateTuningVisually(stringIndex, newTuning);
        }
    }


    /**
     * Converts this panel into a visual drop-zone placeholder during a drag event.
     */
    public void setPlaceholderMode(boolean active) {
        if (dragVisualizer != null) {
            dragVisualizer.setPlaceholderMode(active);
        }
    }
    
    /**
     * Sets the global observer for user actions performed on this panel.
     * * @param observer The implementation handling the action events.
     */
    public void setActionObserver(view.listeners.SongLineActionObserver observer) {
        this.actionObserver = observer;
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
    
    public view.listeners.SongLineActionObserver getActionObserver() { return actionObserver;}
}