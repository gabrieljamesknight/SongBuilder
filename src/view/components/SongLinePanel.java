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
    private Dimension normalSize = null;
    private final javax.swing.border.Border defaultBorder;
    private final javax.swing.border.Border focusedBorder;
    private boolean isPlaceholderActive = false;
    private JPanel innerContentPanel;
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
        this.innerContentPanel = new JPanel(new GridBagLayout());
        this.innerContentPanel.setBackground(new java.awt.Color(45, 48, 52));
        this.innerContentPanel.setOpaque(false);

        this.setBackground(new java.awt.Color(45, 48, 52));
        this.setOpaque(false);

        this.defaultBorder = new javax.swing.border.EmptyBorder(10, 10, 10, 10) {
            @Override
            public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
                // Enable high-quality rendering for smooth, modern corners
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
                
                g2d.setColor(new java.awt.Color(70, 75, 80));
                g2d.drawRoundRect(x, y, width - 1, height - 1, 16, 16);
                g2d.dispose();
            }
        };
        
        /**
         * The active border drawn when any child component has focus.
         * Fetches the theme's native focus color and paints a thicker 2px stroke.
         */
        this.focusedBorder = new javax.swing.border.EmptyBorder(10, 10, 10, 10) {
            @Override
            public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
                
                java.awt.Color focusColor = javax.swing.UIManager.getColor("Component.focusColor");
                g2d.setColor(focusColor != null ? focusColor : new java.awt.Color(62, 134, 224));
                g2d.setStroke(new java.awt.BasicStroke(2.0f));
                
                // Offset by 1px to account for the stroke width and prevent clipping
                g2d.drawRoundRect(x + 1, y + 1, width - 3, height - 3, 16, 16);
                g2d.dispose();
            }
        };

        this.innerContentPanel.setBorder(defaultBorder);

        // --- Initialize Modular Fields ---
        initHeaderComponents();
        initChordsField();
        initTablatureArea();
        initLyricsField(lyricsFont);
        setupFocusTracking();
        
        // --- Setup Panel Focus Tracking ---
        setupFocusTracking();



        
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
            if (this.onRemoveCallback != null) {
                this.onRemoveCallback.accept(this);
            }
        });
        layoutComponents(removeButton);
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
        chordsField.setFont(new Font("Monospaced", Font.PLAIN, 20));
        chordsField.setText(" ".repeat(47));
        chordsField.setHorizontalAlignment(JTextField.LEFT);
        
        LengthFilter lengthFilter = new LengthFilter(50);
        ((AbstractDocument) chordsField.getDocument()).setDocumentFilter(lengthFilter);
        
        // Use the native rounded border and apply necessary padding for text alignment
        chordsField.setBorder(BorderFactory.createCompoundBorder(
            javax.swing.UIManager.getBorder("TextField.border"),
            new EmptyBorder(4, 30, 4, 4)
        ));
        this.chordsHandler = new ChordsInputHandler(chordsField);
    }

    /**
     * Initializes the tablature text area, establishes baseline empty strings, 
     * and attaches its specialized input handler.
     */
    private void initTablatureArea() {
        tablatureArea = new JTextArea() {
            /**
             * Overrides default painting to draw a rounded background.
             * This prevents the default rectangular background from bleeding 
             * through the corners of our rounded border.
             */
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2d.dispose();
                
                super.paintComponent(g);
            }

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
        
        // Disable default rectangular opacity so our custom rounded background shows cleanly
        tablatureArea.setOpaque(false);
        tablatureArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        /**
         * Dynamically fetch FlatLaf's active theme colors for consistency.
         */
        java.awt.Color borderColor = javax.swing.UIManager.getColor("Component.borderColor");
        java.awt.Color focusColor = javax.swing.UIManager.getColor("Component.focusColor");
        
        /**
         * Create responsive borders. The total inset (padding + border thickness) 
         * is kept strictly at 5px in both states to prevent the tablature from shifting 
         * when the component gains focus.
         */
        javax.swing.border.Border unfocusedBorder = new javax.swing.border.EmptyBorder(5, 8, 5, 8) {
            @Override
            public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(borderColor != null ? borderColor : java.awt.Color.GRAY);
                g2d.setStroke(new java.awt.BasicStroke(1.0f));
                g2d.drawRoundRect(x, y, width - 1, height - 1, 12, 12);
                g2d.dispose();
            }
        };

        javax.swing.border.Border focusedBorder = new javax.swing.border.EmptyBorder(5, 8, 5, 8) {
            @Override
            public void paintBorder(java.awt.Component c, java.awt.Graphics g, int x, int y, int width, int height) {
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(focusColor != null ? focusColor : new java.awt.Color(62, 134, 224));
                g2d.setStroke(new java.awt.BasicStroke(2.0f));
                // Offset by 1px to account for the thicker stroke preventing clipping
                g2d.drawRoundRect(x + 1, y + 1, width - 3, height - 3, 12, 12);
                g2d.dispose();
            }
        };

        tablatureArea.setBorder(unfocusedBorder);

        // Attach listener to toggle the focus ring dynamically
        tablatureArea.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                tablatureArea.setBorder(focusedBorder);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                tablatureArea.setBorder(unfocusedBorder);
            }
        });
        
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
        
        // --- Inner Content Panel (The "Card") ---
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        
        // --- COLUMN 0: DRAG HANDLE ---
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        // Allocate 100% of the available empty space on the left to this column
        gbc.weightx = 1.0;
        // Counterbalance the 10px EmptyBorder on the outside by adding 10px to the right
        gbc.insets = new Insets(0, 0, 5, 10);
        gbc.anchor = GridBagConstraints.CENTER;
        innerContentPanel.add(dragHandleLabel, gbc);
        
        // --- CENTER COLUMN ELEMENTS ---
        // Reset weightx to 0 so the center column only takes up exactly its preferred width (600px)
        gbc.weightx = 0.0;
        
        // --- COLUMN 1, ROW 0: Chords ---
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1; 
        /**
         * Add a 5px bottom inset to push the tablature down slightly.
         */
        gbc.insets = new Insets(10, 0, 5, 0); 
        gbc.anchor = GridBagConstraints.WEST;
        innerContentPanel.add(chordsField, gbc);

        // --- COLUMN 1, ROW 1: Tablature Area ---
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 0, 5, 0);
        innerContentPanel.add(tablatureArea, gbc);

        // --- COLUMN 1, ROW 2: Lyrics ---
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1; 
        gbc.insets = new Insets(5, 0, 10, 0); 
        gbc.anchor = GridBagConstraints.WEST;
        innerContentPanel.add(lyricsField, gbc);
        
        // --- COLUMN 2: REMOVE BUTTON ---
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 8, 7, 0); 
        gbc.anchor = GridBagConstraints.CENTER;
        innerContentPanel.add(removeButton, gbc);
        
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
     * Attaches a unified focus listener to all interactive child components.
     */
    private void setupFocusTracking() {
        java.awt.event.FocusAdapter focusAdapter = new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                updatePanelBorder();
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                // invokeLater gives the FocusManager a millisecond to assign the next 
                // focus owner so we don't accidentally drop the highlight when tabbing 
                // between fields in the SAME panel.
                SwingUtilities.invokeLater(() -> updatePanelBorder());
            }
        };

        chordsField.addFocusListener(focusAdapter);
        lyricsField.addFocusListener(focusAdapter);
        tablatureArea.addFocusListener(focusAdapter);
        sectionLabelField.addFocusListener(focusAdapter);
    }

    /**
     * Determines if any child component holds focus and applies the corresponding border.
     */
    private void updatePanelBorder() {
        if (isPlaceholderActive) return;

        java.awt.Component focusOwner = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        boolean hasFocus = SwingUtilities.isDescendingFrom(focusOwner, this);
        
        // Apply focus border to the inner container
        innerContentPanel.setBorder(hasFocus ? focusedBorder : defaultBorder);
        innerContentPanel.repaint();
    }

    /**
     * Converts this panel into a visual drop-zone placeholder during a drag event.
     * This keeps the component in the window hierarchy so mouse focus is never lost.
     */
    public void setPlaceholderMode(boolean active) {
        if (active) {
            normalSize = getSize();
            setPreferredSize(normalSize);
            setMinimumSize(normalSize);
            
            // Hide elements in the inner panel
            for (Component c : innerContentPanel.getComponents()) {
                if (c != dragHandleLabel) {
                    c.setVisible(false);
                }
            }
            sectionLabelField.setVisible(false); // Hide the external label too
            innerContentPanel.setBorder(BorderFactory.createDashedBorder(new Color(100, 130, 200), 3, 5, 2, false));
        } else {
            for (Component c : innerContentPanel.getComponents()) {
                c.setVisible(true);
            }
            sectionLabelField.setVisible(true);
            
            setPreferredSize(null);
            setMinimumSize(null);
            innerContentPanel.setBorder(defaultBorder);
        }
        revalidate();
        repaint();
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