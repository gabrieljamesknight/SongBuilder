package view.listeners;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Encapsulates all input handling (Caret, Keyboard, and Document changes) 
 * for the tablature area to maintain the structural grid.
 */
public class TablatureInputHandler {

    private final JTextArea tablatureArea;
    private boolean isProgrammaticUpdate = false;

    public TablatureInputHandler(JTextArea tablatureArea) {
        this.tablatureArea = tablatureArea;
        attachListeners();
    }

    private void attachListeners() {
        attachCaretListener();
        attachKeyListener();
        attachDocumentListener();
    }

    private boolean isEditablePosition(String text, int pos) {
        int lineStart = text.lastIndexOf('\n', Math.max(0, pos - 1)) + 1;
        int lineEnd = text.indexOf('\n', pos);
        if (lineEnd == -1) {
            lineEnd = text.length();
        }
        return pos >= (lineStart + 4) && pos < (lineEnd - 1);
    }

    private void executeProgrammaticUpdate(Runnable updateAction) {
        SwingUtilities.invokeLater(() -> {
            isProgrammaticUpdate = true;
            try {
                updateAction.run();
            } finally {
                isProgrammaticUpdate = false;
            }
        });
    }

    private void attachCaretListener() {
        tablatureArea.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                if (isProgrammaticUpdate || e.getDot() != e.getMark()) return;

                int dot = e.getDot();
                String text = tablatureArea.getText();
                if (text == null || text.isEmpty()) return;

                int lineStart = text.lastIndexOf('\n', Math.max(0, dot - 1)) + 1;
                int lineEnd = text.indexOf('\n', dot);
                if (lineEnd == -1) lineEnd = text.length();

                int minPos = lineStart + 4; 
                int maxPos = Math.max(minPos, lineEnd - 1); 

                if (dot < minPos) {
                    SwingUtilities.invokeLater(() -> tablatureArea.setCaretPosition(minPos));
                } else if (dot > maxPos) {
                    SwingUtilities.invokeLater(() -> tablatureArea.setCaretPosition(maxPos));
                }
            }
        });
    }

    private void attachKeyListener() {
        tablatureArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                int caretPos = tablatureArea.getCaretPosition();
                String text = tablatureArea.getText();

                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    e.consume();
                    if (caretPos + 1 < text.length()) tablatureArea.setCaretPosition(caretPos + 1);
                    return;
                }

                if (caretPos >= text.length() || Character.isISOControl(e.getKeyChar()) || !isEditablePosition(text, caretPos)) {
                    e.consume();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (tablatureArea.getSelectionStart() != tablatureArea.getSelectionEnd()) {
                    if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) e.consume();
                    return;
                }

                int caretPos = tablatureArea.getCaretPosition();
                String text = tablatureArea.getText();

                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                    if (caretPos > 0 && isEditablePosition(text, caretPos - 1)) {
                        if (text.charAt(caretPos - 1) == '-') {
                            tablatureArea.setCaretPosition(caretPos - 1);
                        } else {
                            String newText = text.substring(0, caretPos - 1) + "-" + text.substring(caretPos);
                            executeProgrammaticUpdate(() -> {
                                tablatureArea.setText(newText);
                                tablatureArea.setCaretPosition(caretPos - 1);
                            });
                        }
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    e.consume();
                    if (caretPos < text.length() && isEditablePosition(text, caretPos)) {
                        if (text.charAt(caretPos) != '-') {
                            String newText = text.substring(0, caretPos) + "-" + text.substring(caretPos + 1);
                            executeProgrammaticUpdate(() -> {
                                tablatureArea.setText(newText);
                                tablatureArea.setCaretPosition(caretPos);
                            });
                        }
                    }
                }
            }
        });
    }

    private void attachDocumentListener() {
        tablatureArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (isProgrammaticUpdate || e.getLength() != 1) return;

                executeProgrammaticUpdate(() -> {
                    String text = tablatureArea.getText();
                    int insertOffset = e.getOffset();
                    int lineEndIndex = text.indexOf('\n', insertOffset);
                    if (lineEndIndex == -1) lineEndIndex = text.length();
                    
                    int hyphenIndex = text.indexOf('-', insertOffset + 1);
                    if (hyphenIndex != -1 && hyphenIndex < lineEndIndex) {
                        text = text.substring(0, hyphenIndex) + text.substring(hyphenIndex + 1);
                        tablatureArea.setText(text);
                        tablatureArea.setCaretPosition(insertOffset + 1);
                    }
                });
            }
            @Override public void removeUpdate(DocumentEvent e) {}
            @Override public void changedUpdate(DocumentEvent e) {}
        });
    }
    
    /**
     * Exposes programmatic updates for external classes (like applying global tuning).
     */
    public void performSafeUpdate(Runnable updateAction) {
        executeProgrammaticUpdate(updateAction);
    }
}