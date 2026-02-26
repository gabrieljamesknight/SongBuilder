package view.listeners;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

/**
 * Encapsulates keyboard and document events for the chords text field.
 * Ensures that the field maintains its fixed-width spatial grid by replacing 
 * deleted characters with spaces and consuming shifting inputs.
 */
public class ChordsInputHandler {

    private static final Logger LOGGER = Logger.getLogger(ChordsInputHandler.class.getName());
    private final JTextField chordsField;

    public ChordsInputHandler(JTextField chordsField) {
        this.chordsField = chordsField;
        attachListeners();
    }

    private void attachListeners() {
        attachKeyListener();
        attachDocumentListener();
    }

    private void attachKeyListener() {
        chordsField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == ' ') {
                     e.consume();
                     return;
                 }

                 int caretPos = chordsField.getCaretPosition();
                 String text = chordsField.getText();
                 
                 // Guarantees there is always a trailing space for the DocumentListener 
                 // to safely consume, preventing text shifting.
                if (caretPos >= text.length() - 1 || 
                    (caretPos < text.length() && text.charAt(caretPos) != ' ' && !Character.isISOControl(e.getKeyChar()))) {
                     e.consume();
                 }
            }

@Override
            public void keyPressed(KeyEvent e) {
                int caretPos = chordsField.getCaretPosition();
                String text = chordsField.getText();

                // Modern Java Rule Switch prevents fall-through bugs and is much cleaner to read
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_BACK_SPACE -> {
                        // Prevent bulk deletion from breaking the grid
                        if (chordsField.getSelectionStart() != chordsField.getSelectionEnd()) {
                            e.consume();
                            return;
                        }
                        
                        if (caretPos > 0) {
                            e.consume();
                            // Hijack the backspace
                            replaceWithSpace(caretPos - 1);
                        } else {
                            e.consume();
                        }
                    }
                        
                    case KeyEvent.VK_DELETE -> {
                        if (chordsField.getSelectionStart() != chordsField.getSelectionEnd()) {
                            e.consume();
                            return;
                        }

                        if (caretPos < text.length()) {
                            e.consume();
                            // Hijack the delete key
                            replaceWithSpace(caretPos);
                        }
                    }
                        
                    case KeyEvent.VK_SPACE -> e.consume();
                        
                    default -> {
                        // Do nothing for other keys
                    }
                }
            }
        });
    }

    private void attachDocumentListener() {
        chordsField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                // Only trim the next space if the user typed a visible chord character.
                if (e.getLength() == 1) {
                     SwingUtilities.invokeLater(() -> {
                        try {
                            int offset = e.getOffset();
                            
                            String text = chordsField.getText();
                            String insertedChar = text.substring(offset, offset + 1);
                            
                            if (!insertedChar.equals(" ") && offset + 1 < text.length() && text.charAt(offset + 1) == ' ') {
                                chordsField.getDocument().remove(offset + 1, 1);
                            }
                        } catch (BadLocationException ex) {
                            LOGGER.log(Level.WARNING, "Failed to maintain chord grid formatting on insert", ex);
                        }
                    });
                }
            }
            
            @Override public void removeUpdate(DocumentEvent e) {}
            @Override public void changedUpdate(DocumentEvent e) {}
        });
    }

    /**
     * Replaces the character at the given position with a space to maintain the grid.
     */
    private void replaceWithSpace(int position) {
        try {
            chordsField.getDocument().remove(position, 1);
            chordsField.getDocument().insertString(position, " ", null);
            chordsField.setCaretPosition(position);
        } catch (BadLocationException ex) {
            LOGGER.log(Level.WARNING, "Failed to replace character with space at position " + position, ex);
        }
    }
}