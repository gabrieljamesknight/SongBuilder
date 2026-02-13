package view;

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


    public SongLinePanel() {
        super();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.songLine = new SongLine();

        Font font = new Font("Arial", Font.PLAIN, 16);

        this.setMaximumSize(new Dimension(600, 200));

        chordsField = new JTextField(50);
        chordsField.setFont(new Font("Monospaced", Font.PLAIN, 20));
        chordsField.setText(" ".repeat(49));
        chordsField.setHorizontalAlignment(JTextField.LEFT);
        LengthFilter lengthFilter = new LengthFilter(50);
        ((AbstractDocument) chordsField.getDocument()).setDocumentFilter(lengthFilter);

        this.add(chordsField);
        chordsField.setBorder(new EmptyBorder(0, 30, 0, 0));
        chordsField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    int caretPos = chordsField.getCaretPosition();
                    String text = chordsField.getText();
        
                    if (caretPos > 1 && text.charAt(caretPos-1) != ' ') {
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

            public void removeUpdate(DocumentEvent e) {
            }
            
            public void changedUpdate(DocumentEvent e) {
            }

        });
        
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
        
        tablatureArea.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                int caretPos = tablatureArea.getCaretPosition();
                String text = tablatureArea.getText();
                if (caretPos >= text.length() || text.charAt(caretPos) != '-') {
                    e.consume();
                }
            }
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    int caretPos = tablatureArea.getCaretPosition();
                    String text = tablatureArea.getText();
            
                    if (text.charAt(caretPos) != '-' || text.charAt(caretPos-1)=='|') {
                        e.consume();
                    }
                }
            }
        });


        tablatureArea.getDocument().addDocumentListener(new DocumentListener() {
            private boolean ignore = false;
        
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        String text = tablatureArea.getText();
                        int insertLength = e.getLength();
                        int insertOffset = e.getOffset();
                        int index = text.indexOf('-', insertOffset + insertLength);
                        if (index != -1) {
                            text = text.substring(0, index) + text.substring(index + 1);
                            tablatureArea.setText(text);
                            tablatureArea.setCaretPosition(insertOffset+1);
                        }
                    }
                });
            }
        
            public void removeUpdate(DocumentEvent e) {
                if (ignore) return;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        ignore = true;
                        String text = tablatureArea.getText();
                        int removeOffset = e.getOffset();
                        if (removeOffset < text.length() && text.charAt(removeOffset) == '-') {
                            text = text.substring(0, removeOffset) + "-" + text.substring(removeOffset);
                            tablatureArea.setText(text);
                            tablatureArea.setCaretPosition(removeOffset);
                        }
                        ignore = false;
                    }
                });

            }
            
        
            public void changedUpdate(DocumentEvent e) {
            }
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

