package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;

import model.SongLine;
import model.Tablature;


public class SongLinePanel extends JPanel {
    private JTextField chordsField, lyricsField;
    private JTextArea tablatureArea;
    private SongLine songLine;
    private boolean isProgrammaticUpdate = false;
    private Consumer<SongLinePanel> onRemoveCallback;


    public SongLinePanel() {
        super();
        this.setLayout(new GridBagLayout());
        this.songLine = new SongLine();
        Font font = new Font("Arial", Font.PLAIN, 16);

        this.setMaximumSize(new Dimension(850, 200));

        chordsField = new JTextField();
        chordsField.setFont(new Font("Monospaced", Font.PLAIN, 20));
        chordsField.setText(" ".repeat(48));
        chordsField.setHorizontalAlignment(JTextField.LEFT);
        LengthFilter lengthFilter = new LengthFilter(49);
        ((AbstractDocument) chordsField.getDocument()).setDocumentFilter(lengthFilter);
        chordsField.setBorder(new EmptyBorder(0, 30, 0, 0));

        
        // --- CHORD FIELD LOGIC ---
        chordsField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                 int caretPos = chordsField.getCaretPosition();
                 String text = chordsField.getText();
                 
                 // This guarantees there is always a trailing space for the DocumentListener 
                 // to safely consume, preventing the text from shifting or getting locked.
                if (caretPos >= text.length() - 1 || 
                    (caretPos < text.length() && text.charAt(caretPos) != ' ' && !Character.isISOControl(e.getKeyChar()))) {
                     e.consume();
                 }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                int caretPos = chordsField.getCaretPosition();
                String text = chordsField.getText();

                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    // Prevent bulk deletion from breaking the grid
                    if (chordsField.getSelectionStart() != chordsField.getSelectionEnd()) {
                        e.consume();
                        return;
                    }
                    
                    if (caretPos > 0) {
                        e.consume(); // Hijack the backspace
                        try {
                            // Explicitly replace the preceding character with a space
                            chordsField.getDocument().remove(caretPos - 1, 1);
                            chordsField.getDocument().insertString(caretPos - 1, " ", null);
                            chordsField.setCaretPosition(caretPos - 1);
                        } catch (BadLocationException ex) {
                            ex.printStackTrace(); // IDE Warning Fix
                        }
                    } else {
                        e.consume();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    if (chordsField.getSelectionStart() != chordsField.getSelectionEnd()) {
                        e.consume();
                        return;
                    }

                    if (caretPos < text.length()) {
                        e.consume(); // Hijack the delete key
                        try {
                            chordsField.getDocument().remove(caretPos, 1);
                            chordsField.getDocument().insertString(caretPos, " ", null);
                            chordsField.setCaretPosition(caretPos);
                        } catch (BadLocationException ex) {
                            ex.printStackTrace(); // IDE Warning Fix
                        }
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    e.consume();
                }
            }
        });

        // DocumentListener for Chords
        chordsField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                // Only trim the next space if the user typed a visible chord character.
                // This stops programmatic space replacements (from backspace) from shrinking the field
                if (e.getLength() == 1) {
                    SwingUtilities.invokeLater(() -> { // IDE Warning Fix: converted to lambda
                        try {
                            int offset = e.getOffset();
                            String text = chordsField.getText();
                            String insertedChar = text.substring(offset, offset + 1);
                            
                            if (!insertedChar.equals(" ") && offset + 1 < text.length() && text.charAt(offset + 1) == ' ') {
                                chordsField.getDocument().remove(offset + 1, 1);
                            }
                        } catch (BadLocationException ex) {
                            ex.printStackTrace(); // IDE Warning Fix
                        }
                    });
                }
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {}
            
            @Override
            public void changedUpdate(DocumentEvent e) {}
        });

        // --- TABLATURE LOGIC ---
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
        // Remove the hardcoded column count (40)
        lyricsField = new JTextField();
        lyricsField.setFont(font);
        Tablature emptyTablature = new Tablature();
        tablatureArea.setText(emptyTablature.toString());
        
        LengthFilter lyricLengthFilter = new LengthFilter(65);
        ((AbstractDocument) lyricsField.getDocument()).setDocumentFilter(lyricLengthFilter);
        

        
        JButton removeButton = new JButton("🗑");
        removeButton.setToolTipText("Remove this line");
        removeButton.setFocusable(false); 
        removeButton.addActionListener(e -> {
            if (this.onRemoveCallback != null) {
                this.onRemoveCallback.accept(this);
            }
        });
    



        // --- TABLATURE CARET SNAP LOGIC ---
        // Ensures the cursor can never be placed inside the structural boundaries
        tablatureArea.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {

                if (isProgrammaticUpdate) return;

                // Ignore if the user is actively highlighting text
                if (e.getDot() != e.getMark()) return;

                int dot = e.getDot();
                String text = tablatureArea.getText();
                
                if (text == null || text.isEmpty()) return;

                // Calculate the boundaries of the current line the caret is on
                int lineStart = text.lastIndexOf('\n', Math.max(0, dot - 1)) + 1;
                int lineEnd = text.indexOf('\n', dot);
                if (lineEnd == -1) {
                    lineEnd = text.length();
                }

                // Define the strict editable zone: after the 4-char prefix, before the 1-char suffix
                int minPos = lineStart + 4; 
                int maxPos = Math.max(minPos, lineEnd - 1); 

                // Snap the caret back into bounds if it wanders outside
                if (dot < minPos) {
                    SwingUtilities.invokeLater(() -> tablatureArea.setCaretPosition(minPos));
                } else if (dot > maxPos) {
                    SwingUtilities.invokeLater(() -> tablatureArea.setCaretPosition(maxPos));
                }
            }
        });
        
        // Tablature Key Listener
        tablatureArea.addKeyListener(new KeyAdapter() {
            
            /**
             * Helper method to determine if a given text index is within the editable tablature area.
             * Protects the 4-character prefix (e.g., "e ||") and the 1-character suffix ("|").
             */
            private boolean isEditablePosition(String text, int pos) {
                int lineStart = text.lastIndexOf('\n', pos - 1) + 1;
                int lineEnd = text.indexOf('\n', pos);
                if (lineEnd == -1) {
                    lineEnd = text.length();
                }
                
                // Editable area strictly starts after the 4-character prefix and ends before the 1-character suffix
                return pos >= (lineStart + 4) && pos < (lineEnd - 1);
            }

            @Override
            public void keyTyped(KeyEvent e) {
                int caretPos = tablatureArea.getCaretPosition();
                String text = tablatureArea.getText();

                // Prevent typing out of bounds or typing control characters
                if (caretPos >= text.length() || Character.isISOControl(e.getKeyChar())) {
                    e.consume();
                    return;
                }

                // Strictly enforce typing only within the editable tablature area
                if (!isEditablePosition(text, caretPos)) {
                    e.consume();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                // Prevent multi-character selection deletion to preserve the structural grid
                if (tablatureArea.getSelectionStart() != tablatureArea.getSelectionEnd()) {
                    if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) {
                        e.consume();
                    }
                    return;
                }

                int caretPos = tablatureArea.getCaretPosition();
                String text = tablatureArea.getText();

                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    e.consume(); // Hijack the backspace

                    if (caretPos > 0) {
                        if (!isEditablePosition(text, caretPos - 1)) return;

                        char charBefore = text.charAt(caretPos - 1);

                        if (charBefore == '-') {
                            tablatureArea.setCaretPosition(caretPos - 1);
                        } else {
                            String newText = text.substring(0, caretPos - 1) + "-" + text.substring(caretPos);
                            
                            SwingUtilities.invokeLater(() -> {
                                isProgrammaticUpdate = true; // Lock listeners
                                try {
                                    tablatureArea.setText(newText);
                                    tablatureArea.setCaretPosition(caretPos - 1);
                                } finally {
                                    isProgrammaticUpdate = false; // Unlock listeners
                                }
                            });
                        }
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    e.consume(); // Hijack the delete key

                    if (caretPos < text.length()) {
                        if (!isEditablePosition(text, caretPos)) return;

                        char charAt = text.charAt(caretPos);

                        if (charAt != '-') {
                            String newText = text.substring(0, caretPos) + "-" + text.substring(caretPos + 1);
                            SwingUtilities.invokeLater(() -> {
                                isProgrammaticUpdate = true; // Lock listeners
                                try {
                                    tablatureArea.setText(newText);
                                    tablatureArea.setCaretPosition(caretPos);
                                } finally {
                                    isProgrammaticUpdate = false; // Unlock listeners
                                }
                            });
                        }
                    }
                }
            }
        });

        // Tablature Document Listener
        tablatureArea.getDocument().addDocumentListener(new DocumentListener() {
           @Override
            public void insertUpdate(DocumentEvent e) {
                // Ignore if the KeyListener is currently fixing the grid, or if it's a bulk paste
                if (isProgrammaticUpdate || e.getLength() != 1) return;

                SwingUtilities.invokeLater(() -> {
                    isProgrammaticUpdate = true; // Lock listeners
                    try {
                        String text = tablatureArea.getText();
                        int insertOffset = e.getOffset();
                        
                        int lineEndIndex = text.indexOf('\n', insertOffset);
                        if (lineEndIndex == -1) {
                            lineEndIndex = text.length();
                        }
                        
                        int hyphenIndex = text.indexOf('-', insertOffset + 1);
                        if (hyphenIndex != -1 && hyphenIndex < lineEndIndex) {
                            text = text.substring(0, hyphenIndex) + text.substring(hyphenIndex + 1);
                            tablatureArea.setText(text);
                            tablatureArea.setCaretPosition(insertOffset + 1);
                        }
                    } finally {
                        isProgrammaticUpdate = false; // Unlock listeners
                    }
                });
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                // Intentionally left blank.
                // Grid preservation during deletion is handled by intercepting Backspace/Delete.
            }

            @Override
            public void changedUpdate(DocumentEvent e) {}
        });
        
        lyricsField.setBorder(new EmptyBorder(0, 30, 0, 0));
        

        chordsField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 2),
            new EmptyBorder(0, 30, 0, 0)
        ));

        // --- 1. ENFORCE UNIFORM SIZES ---
        // Lock the width strictly to 600px for all three fields to perfectly align them.
        int targetWidth = 600;
        Dimension chordsDim = new Dimension(targetWidth, 35);
        Dimension tabDim    = new Dimension(targetWidth, 100);
        Dimension lyricsDim = new Dimension(targetWidth, 26);
        
        // Apply iron-clad sizing to Chords
        chordsField.setPreferredSize(chordsDim);
        chordsField.setMinimumSize(chordsDim);
        chordsField.setMaximumSize(chordsDim);
        
        // Apply iron-clad sizing to Tablature
        tablatureArea.setPreferredSize(tabDim);
        tablatureArea.setMinimumSize(tabDim);
        tablatureArea.setMaximumSize(tabDim);
        
        // Apply iron-clad sizing to Lyrics
        lyricsField.setPreferredSize(lyricsDim);
        lyricsField.setMinimumSize(lyricsDim);
        lyricsField.setMaximumSize(lyricsDim);

        // --- 2. GRIDBAG PLACEMENT ---
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Set fill to NONE so the layout manager cannot override our strict pixel sizes above
        gbc.fill = GridBagConstraints.NONE; 

        // Chords (Top Row, Center Column)
        gbc.gridx = 1;
        gbc.gridy = 0; 
        gbc.insets = new Insets(0, 0, 5, 0); // 5px gap below
        gbc.anchor = GridBagConstraints.CENTER;
        this.add(chordsField, gbc);

        // Tablature (Middle Row, Center Column)
        gbc.gridy = 1; 
        gbc.anchor = GridBagConstraints.CENTER; 
        this.add(tablatureArea, gbc);

        // Bin Button (Middle Row, Right Column)
        gbc.gridx = 2; 
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 15, 5, 0); // 15px gap to the left of the button
        gbc.anchor = GridBagConstraints.WEST;
        this.add(removeButton, gbc);

        // Lyrics (Bottom Row, Center Column)
        gbc.gridx = 1; 
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 15, 0); // 15px gap below to separate song lines
        gbc.anchor = GridBagConstraints.CENTER;
        this.add(lyricsField, gbc);

        // Dummy Spacer (Middle Row, Left Column)
        // Invisibly counter-balances the bin button's width on the right
        Component dummy = Box.createRigidArea(new Dimension(55, 0));
        gbc.gridx = 0; 
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        this.add(dummy, gbc);
    }

    /**
     * Safely updates the tuning prefix for a specific guitar string in the UI text area.
     * Prevents triggering the DocumentListener by locking the programmatic update flag.
     * * @param stringIndex The zero-based index of the guitar string (0 = thin 'e', 5 = thick 'E').
     * @param newTuning The new tuning string to display (e.g., "D", "D#").
     */
    public void updateTuningVisually(int stringIndex, String newTuning) {
        // Ensure standard 2-character formatting for alignment
        String formattedTuning = String.format("%-2s", newTuning);
        String currentText = tablatureArea.getText();
        String[] lines = currentText.split("\n");
        
        if (stringIndex >= 0 && stringIndex < lines.length) {
            // Replace only the first 2 characters of the specific line
            if (lines[stringIndex].length() >= 2) {
                lines[stringIndex] = formattedTuning + lines[stringIndex].substring(2);
                
                SwingUtilities.invokeLater(() -> {
                    isProgrammaticUpdate = true; // Lock listeners
                    try {
                        tablatureArea.setText(String.join("\n", lines));
                    } finally {
                        isProgrammaticUpdate = false; // Unlock listeners
                    }
                });
            }
        }
    }

    /**
     * Sets the callback to be triggered when the remove button is clicked.
     *
     * @param callback The function to execute, passing this panel as the argument.
     */
    public void setOnRemoveCallback(Consumer<SongLinePanel> callback) {
        this.onRemoveCallback = callback;
    }

    public void updateSongLine() {
        songLine.setChords(getChords());
        songLine.setLyrics(getLyrics());
        songLine.setTablature(getTablature());
    }

    public SongLine getSongLine() {
        return songLine;
    }

    public String getChords() {
        return chordsField.getText();
    }

    public String getLyrics() {
        return lyricsField.getText();
    }

    public Tablature getTablature() {
        return Tablature.parseTablature(tablatureArea.getText());
    }

    public JTextField getChordsField() {
        return chordsField;
    }
    
    public JTextField getLyricsField() {
        return lyricsField;
    }
    
    public JTextArea getTablatureArea() {
        return tablatureArea;
    }
    
    
}

