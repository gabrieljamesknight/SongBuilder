package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.AttributeSet;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import model.SongLine;
import model.Tablature;

/**
 * Represents a single line of a song, containing Chords, Tablature, and Lyrics.
 * Refactored to have a FIXED width (like a sheet of paper) and prevented vertical stretching.
 */
public class SongLinePanel extends JPanel {
    
    // UI Components
    private JTextField chordsField;
    private JTextField lyricsField;
    private JTextArea tablatureArea;
    
    // Data Model
    private SongLine songLine;

    // Layout Constants
    private static final int FIXED_WIDTH = 780; // Fixed width for the song line
    private static final int FIXED_HEIGHT = 280; // Approximate height for one line panel

    // Custom Colors for Dark Mode Contrast
    private final Color INPUT_BACKGROUND = new Color(45, 45, 45);
    private final Color CHORD_TEXT_COLOR = new Color(255, 200, 100); // Gold/Orange
    private final Color LYRIC_TEXT_COLOR = new Color(100, 200, 255); // Light Blue
    private final Color TAB_TEXT_COLOR = new Color(220, 220, 220); // Off-White

    public SongLinePanel() {
        super();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.songLine = new SongLine();

        // --- 1. VISUAL SETUP: Fixed Size & Border ---
        // Force the panel to stay at a specific width, preventing window resize from affecting it
        Dimension fixedDim = new Dimension(FIXED_WIDTH, FIXED_HEIGHT);
        this.setPreferredSize(fixedDim);
        this.setMaximumSize(fixedDim); 
        this.setMinimumSize(fixedDim);
        
        this.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(10, 10, 10, 10), 
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80), 1, true), 
                "Song Line", 
                TitledBorder.LEFT, 
                TitledBorder.TOP, 
                new Font("Arial", Font.BOLD, 12),
                Color.GRAY
            )
        ));
        this.setOpaque(false);
        
        // --- 2. CHORDS FIELD SETUP ---
        chordsField = new JTextField(); 
        chordsField.setFont(new Font("Monospaced", Font.BOLD, 16));
        chordsField.setText(" ".repeat(84)); 
        chordsField.setHorizontalAlignment(JTextField.LEFT);
        
        // Styling
        chordsField.setBackground(INPUT_BACKGROUND);
        chordsField.setForeground(CHORD_TEXT_COLOR);
        chordsField.setCaretColor(Color.WHITE);
        chordsField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // FIX: Prevent Vertical Stretching
        // We set Max Width to MAX_VALUE (so it fills the panel width)
        // We set Max Height to the PREFERRED height (so it never grows taller)
        chordsField.setMaximumSize(new Dimension(Integer.MAX_VALUE, chordsField.getPreferredSize().height));
        
        ((AbstractDocument) chordsField.getDocument()).setDocumentFilter(new LengthFilter(100));

        // --- 3. TABLATURE AREA SETUP ---
        // Use rows/columns to establish the "natural" size
        tablatureArea = new JTextArea(6, 85); 
        tablatureArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        tablatureArea.setLineWrap(false);
        
        // Styling
        tablatureArea.setBackground(INPUT_BACKGROUND);
        tablatureArea.setForeground(TAB_TEXT_COLOR);
        tablatureArea.setCaretColor(Color.WHITE);
        tablatureArea.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        
        Tablature emptyTablature = new Tablature();
        tablatureArea.setText(emptyTablature.toString());
        
        // --- 4. LYRICS FIELD SETUP ---
        lyricsField = new JTextField();
        lyricsField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        // Styling
        lyricsField.setBackground(INPUT_BACKGROUND);
        lyricsField.setForeground(LYRIC_TEXT_COLOR);
        lyricsField.setCaretColor(Color.WHITE);
        lyricsField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // FIX: Prevent Vertical Stretching for Lyrics too
        lyricsField.setMaximumSize(new Dimension(Integer.MAX_VALUE, lyricsField.getPreferredSize().height));
        
        ((AbstractDocument) lyricsField.getDocument()).setDocumentFilter(new LengthFilter(100));

        // --- 5. ADDING COMPONENTS TO PANEL ---
        this.add(createLabel("Chords"));
        this.add(chordsField);
        
        this.add(Box.createVerticalStrut(8));
        
        this.add(createLabel("Tablature"));
        
        // Wrap Tablature in ScrollPane
        JScrollPane tabScroll = new JScrollPane(tablatureArea);
        tabScroll.setBorder(null);
        tabScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tabScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        
        // Ensure the ScrollPane (and thus the tab area) fills the width but respects height
        tabScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, tabScroll.getPreferredSize().height));
        this.add(tabScroll);
        
        this.add(Box.createVerticalStrut(8));
        
        this.add(createLabel("Lyrics"));
        this.add(lyricsField);
        
        this.add(Box.createVerticalStrut(10));

        // --- 6. ATTACH LOGIC LISTENERS ---
        setupChordsListeners();
        setupTablatureListeners();
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 10));
        label.setForeground(Color.GRAY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void setupChordsListeners() {
        chordsField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    int caretPos = chordsField.getCaretPosition();
                    String text = chordsField.getText();
                    if (caretPos > 0 && caretPos <= text.length()) {
                        if (text.charAt(caretPos - 1) != ' ') {
                            try {
                                StringBuilder sb = new StringBuilder(text);
                                sb.setCharAt(caretPos - 1, ' ');
                                chordsField.setText(sb.toString());
                                chordsField.setCaretPosition(caretPos - 1);
                                e.consume();
                            } catch (Exception ex) { ex.printStackTrace(); }
                        }
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    e.consume();
                }
            }
        });
        chordsField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        int offset = e.getOffset();
                        String text = chordsField.getText();
                        if (offset + 1 < text.length() && text.charAt(offset + 1) == ' ') {
                            ((AbstractDocument)chordsField.getDocument()).remove(offset + 1, 1);
                        }
                    } catch (BadLocationException ex) { ex.printStackTrace(); }
                });
            }
            public void removeUpdate(DocumentEvent e) {}
            public void changedUpdate(DocumentEvent e) {}
        });
    }

    private void setupTablatureListeners() {
        tablatureArea.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                int caretPos = tablatureArea.getCaretPosition();
                String text = tablatureArea.getText();
                if (caretPos >= text.length() || (text.charAt(caretPos) != '-' && text.charAt(caretPos) != '|')) {
                   // Logic for restricting input can go here
                }
            }
            
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    int caretPos = tablatureArea.getCaretPosition();
                    String text = tablatureArea.getText();
                    
                    if (caretPos > 0 && (text.charAt(caretPos - 1) == '|' || text.charAt(caretPos - 1) == '\n')) {
                        e.consume();
                        return;
                    }
                    
                    if (caretPos > 0) {
                        try {
                            StringBuilder sb = new StringBuilder(text);
                            sb.setCharAt(caretPos - 1, '-');
                            tablatureArea.setText(sb.toString());
                            tablatureArea.setCaretPosition(caretPos - 1);
                            e.consume();
                        } catch (Exception ex) {}
                    }
                }
                if ((e.getKeyCode() == KeyEvent.VK_C || e.getKeyCode() == KeyEvent.VK_V) && e.isControlDown()) {
                    e.consume();
                }
            }
        });
        tablatureArea.getDocument().addDocumentListener(new DocumentListener() {
            private boolean ignore = false;
            public void insertUpdate(DocumentEvent e) {
                if (ignore) return;
                SwingUtilities.invokeLater(() -> {
                    ignore = true;
                    try {
                        String text = tablatureArea.getText();
                        int offset = e.getOffset() + e.getLength();
                        int nextDash = text.indexOf('-', offset);
                        int nextLineBreak = text.indexOf('\n', offset);
                        
                        if (nextDash != -1 && (nextLineBreak == -1 || nextDash < nextLineBreak)) {
                            tablatureArea.replaceRange("", nextDash, nextDash + 1);
                            tablatureArea.setCaretPosition(offset);
                        }
                    } catch (Exception ex) { ex.printStackTrace(); }
                    ignore = false;
                });
            }
            public void removeUpdate(DocumentEvent e) {}
            public void changedUpdate(DocumentEvent e) {}
        });
    }

    // --- GETTERS & SETTERS ---
    public void updateSongLine() {
        songLine.setChords(getChords());
        songLine.setLyrics(getLyrics());
        songLine.setTablature(getTablature());
    }

    public SongLine getSongLine() { return songLine; }
    public String getChords() { return chordsField.getText(); }
    public String getLyrics() { return lyricsField.getText(); }
    public Tablature getTablature() { return Tablature.parseTablature(tablatureArea.getText()); }
    public JTextField getChordsField() { return chordsField; }
    public JTextField getLyricsField() { return lyricsField; }
    public JTextArea getTablatureArea() { return tablatureArea; }
}