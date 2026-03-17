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
import controller.SongLineMapper;
import javax.swing.BorderFactory;
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
    private JLabel lineNumberLabel;
    private JLabel dragHandleLabel;
    private TablatureTextArea tablatureArea;
    private final SongLine songLine;
    protected boolean isProgrammaticUpdate = false;
    private FocusAwareBorderPanel innerContentPanel;
    private SongLineActionObserver actionObserver;
    private ChordsInputHandler chordsHandler;
    private TablatureInputHandler tablatureHandler;
    private LyricsInputHandler lyricsHandler;
    private Dimension normalSize = null;
    private boolean isPlaceholderActive = false;    
    Dimension iconBox = new Dimension(40, 40);
    

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
        this.innerContentPanel = new FocusAwareBorderPanel(createDefaultBorder(), createFocusedBorder());
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

        /** * Inject the custom Java2D icon. 
         * Using a modern alert red (e.g., #DC3545) for the flat aesthetic.
         */
        HoverButton removeButton = new HoverButton();
        removeButton.setIcon(new TrashIcon(30, 35, new java.awt.Color(180, 40, 55)));
        removeButton.setToolTipText("Remove this line");
        removeButton.setFocusable(false); 
        
        // Strip out the default Swing button background/border to keep it flat
        removeButton.setContentAreaFilled(false);
        removeButton.setBorderPainted(false);
        removeButton.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        removeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        removeButton.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        removeButton.putClientProperty("JComponent.minimumWidth", 0);
        removeButton.setPreferredSize(new Dimension(40, 40));

        removeButton.setPreferredSize(iconBox);
        removeButton.setMinimumSize(iconBox);
        removeButton.setMaximumSize(iconBox);
        
        removeButton.addActionListener(e -> {
            if (this.actionObserver != null) {
                this.actionObserver.onRemove(this);
            }
        });

        initContextMenu();
        layoutComponents(removeButton);

        this.innerContentPanel.attachFocusTracking(this.innerContentPanel);
    }

    private void initHeaderComponents() {
        lineNumberLabel = new JLabel("# ");
        lineNumberLabel.setFont(new Font("Arial", Font.BOLD, 18));
        lineNumberLabel.setForeground(new Color(150, 150, 150));

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
        
        // Strip hidden default margins from the JButton to prevent visual offset
        removeButton.setMargin(new Insets(0, 0, 0, 0));
        removeButton.setBorder(BorderFactory.createEmptyBorder());

        // 1. Line Number Label Styling (Outside the Box)
        lineNumberLabel.setHorizontalAlignment(JLabel.CENTER);
        lineNumberLabel.setFont(new Font("Arial", Font.BOLD, 28)); 

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
        dragHandleLabel = new JLabel();
        dragHandleLabel.setName("dragHandle");
        dragHandleLabel.setIcon(new DragHandleIcon(30, 35, new Color(100, 100, 100)));
        dragHandleLabel.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        dragHandleLabel.setToolTipText("Click and drag to reorder this line");
        dragHandleLabel.setPreferredSize(iconBox);
        dragHandleLabel.setHorizontalAlignment(JLabel.CENTER);
        dragHandleLabel.setMinimumSize(iconBox);
        dragHandleLabel.setMaximumSize(iconBox);
        
        
        // --- Left Margin Panel (Contains ONLY the Line Number) ---
        JPanel leftMarginPanel = new JPanel(new GridBagLayout());
        leftMarginPanel.setOpaque(false);
        GridBagConstraints marginGbc = new GridBagConstraints();
        marginGbc.insets = new Insets(4, 0, 0, 15);
        marginGbc.anchor = GridBagConstraints.CENTER;
        leftMarginPanel.add(lineNumberLabel, marginGbc);
        
        // COLUMN 0: Drag Handle
        innerContentPanel.add(dragHandleLabel, GridBagHelper.createConstraints(0, 1, 1.0, new Insets(0, 0, 5, 10), GridBagConstraints.CENTER));
        
        // COLUMN 1: Chords, Tablature, and Lyrics (weightx = 0.0 to maintain preferred width)
        innerContentPanel.add(chordsField, GridBagHelper.createConstraints(1, 0, 0.0, new Insets(10, 0, 5, 0), GridBagConstraints.WEST));
        innerContentPanel.add(tablatureArea, GridBagHelper.createConstraints(1, 1, 0.0, new Insets(5, 0, 5, 0), GridBagConstraints.WEST));
        innerContentPanel.add(lyricsField, GridBagHelper.createConstraints(1, 2, 0.0, new Insets(5, 0, 10, 0), GridBagConstraints.WEST));
        
        // COLUMN 2: Remove Button
        innerContentPanel.add(removeButton, GridBagHelper.createConstraints(2, 1, 1.0, new Insets(0, 8, 7, 0), GridBagConstraints.CENTER));
        
        // Assemble the outer panel
        this.add(sectionLabelField, java.awt.BorderLayout.NORTH);
        this.add(leftMarginPanel, java.awt.BorderLayout.WEST); 
        this.add(innerContentPanel, java.awt.BorderLayout.CENTER);
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


    /**
     * Converts this panel into a visual drop-zone placeholder during a drag event.
     * This keeps the component in the window hierarchy so mouse focus is never lost.
     */
    public void setPlaceholderMode(boolean active) {
        this.isPlaceholderActive = active; // Clears the hint!
        if (active) {
            normalSize = getSize();
            setPreferredSize(normalSize);
            setMinimumSize(normalSize);
            
            for (Component c : innerContentPanel.getComponents()) {
                if (c != dragHandleLabel) {
                    c.setVisible(false);
                }
            }
            sectionLabelField.setVisible(false);
            innerContentPanel.setBorder(BorderFactory.createDashedBorder(new Color(100, 130, 200), 3, 5, 2, false));
        } else {
            for (Component c : innerContentPanel.getComponents()) {
                c.setVisible(true);
            }
            sectionLabelField.setVisible(true);
            
            setPreferredSize(null);
            setMinimumSize(null);
            
            // Tell the border panel to resume normal focus tracking
            innerContentPanel.setPlaceholderMode(false); 
        }
        revalidate();
        repaint();
    }

    public void setLineNumber(int number) {
        lineNumberLabel.setText(String.valueOf(number) + ". ");
    }

    private javax.swing.border.Border createDefaultBorder() {
    return new javax.swing.border.EmptyBorder(10, 10, 10, 10) {
        @Override
        public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
            java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new java.awt.Color(70, 75, 80));
            g2d.drawRoundRect(x, y, width - 1, height - 1, 16, 16);
            g2d.dispose();
        }
    };
}

    private javax.swing.border.Border createFocusedBorder() {
    return new javax.swing.border.EmptyBorder(10, 10, 10, 10) {
        @Override
        public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
            java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            java.awt.Color focusColor = javax.swing.UIManager.getColor("Component.focusColor");
            g2d.setColor(focusColor != null ? focusColor : new java.awt.Color(62, 134, 224));
            g2d.setStroke(new java.awt.BasicStroke(2.0f));
            g2d.drawRoundRect(x + 1, y + 1, width - 3, height - 3, 16, 16);
            g2d.dispose();
        }
    };
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

    public view.listeners.SongLineActionObserver getActionObserver() { 
        return actionObserver; 
    }
}