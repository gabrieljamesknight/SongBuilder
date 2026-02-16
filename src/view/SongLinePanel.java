package view;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.SwingUtilities;
import model.SongLine;
import model.Tablature;


public class SongLinePanel extends JPanel {
    private JTextField chordsField, lyricsField;
    private JTextArea tablatureArea;
    private SongLine songLine;
    private boolean isProgrammaticUpdate = false;


    public SongLinePanel() {
        super();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.songLine = new SongLine();
        Font font = new Font("Arial", Font.PLAIN, 16);

        this.setMaximumSize(new Dimension(600, 200));

        chordsField = new JTextField(50);
        chordsField.setFont(new Font("Monospaced", Font.PLAIN, 20));
        chordsField.setText(" ".repeat(48));
        chordsField.setHorizontalAlignment(JTextField.LEFT);
        LengthFilter lengthFilter = new LengthFilter(49);
        ((AbstractDocument) chordsField.getDocument()).setDocumentFilter(lengthFilter);

        this.add(chordsField);
        chordsField.setBorder(new EmptyBorder(0, 30, 0, 0));
        
        // --- CHORD FIELD LOGIC (Modified) ---
        chordsField.addKeyListener(new KeyAdapter() {
            // NEW: Consume input if the position is already occupied
            public void keyTyped(KeyEvent e) {
                 int caretPos = chordsField.getCaretPosition();
                 String text = chordsField.getText();
                 
                 // If the char at this position is NOT a space, block the input
                if (caretPos >= 48 || 
                    (caretPos < text.length() && text.charAt(caretPos) != ' ' && !Character.isISOControl(e.getKeyChar()))) {
                     e.consume();
                 }

                 
            }

            // Original Backspace Logic [cite: 16]
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    int caretPos = chordsField.getCaretPosition();
                    String text = chordsField.getText();

                    if (caretPos > 1 && text.charAt(caretPos - 1) != ' ') {
                        StringBuilder sb = new StringBuilder(text);
                        sb.insert(caretPos, ' ');
                        chordsField.setText(sb.toString());
                        chordsField.setCaretPosition(caretPos);
                    } else {
                        e.consume();
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    e.consume();
                }
            }
        });

        // Original DocumentListener for Chords
        chordsField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            int offset = e.getOffset();
                            String text = chordsField.getText();
                            if (offset != 0 && text.charAt(offset + 1) == ' ') {
                                chordsField.getDocument().remove(offset + 1, 1);
                            }
                        } catch (BadLocationException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
            public void removeUpdate(DocumentEvent e) {}
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
        tablatureArea.setBorder(new EmptyBorder(10, 5, 10, 10));
        lyricsField = new JTextField(40);
        lyricsField.setFont(font);
        Tablature emptyTablature = new Tablature();
        tablatureArea.setText(emptyTablature.toString());
        
        LengthFilter lyricLengthFilter = new LengthFilter(65);
        ((AbstractDocument) lyricsField.getDocument()).setDocumentFilter(lyricLengthFilter);
        
        JLabel tablatureLabel = new JLabel(" ");
        tablatureLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        tablatureArea.setMargin(new Insets(0, 100, 0, 0));
        this.add(tablatureArea);

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
            private boolean ignore = false;

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
                // Grid preservation during deletion is now strictly handled by 
                // intercepting Backspace/Delete in the KeyListener.
            }

            @Override
            public void changedUpdate(DocumentEvent e) {}
        });

        JLabel lyricsLabel = new JLabel(" ");
        lyricsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(lyricsLabel);
        this.add(lyricsField);
        lyricsField.setBorder(new EmptyBorder(0, 30, 0, 0));
        chordsField.setBorder(BorderFactory.createLineBorder(Color.BLACK,2));
        this.add(new Box.Filler(new Dimension(0, 20), new Dimension(0, 20), new Dimension(0, Short.MAX_VALUE)));
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

