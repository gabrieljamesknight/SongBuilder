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
import javax.swing.JButton;
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
import view.listeners.TablatureInputHandler;

public class SongLinePanel extends JPanel {
    private JTextField chordsField, lyricsField;
    private JTextArea tablatureArea;
    private SongLine songLine;
    protected boolean isProgrammaticUpdate = false;
    private Consumer<SongLinePanel> onRemoveCallback;

    public SongLinePanel() {
        super();
        this.setLayout(new GridBagLayout());
        this.songLine = new SongLine();
        Font font = new Font("Arial", Font.PLAIN, 16);

        this.setMaximumSize(new Dimension(850, 200));

        // --- Initialize Modular Fields ---
        initChordsField();
        initTablatureArea();
        initLyricsField(font);

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

        // WIRE UP THE NEW HANDLER:
        new ChordsInputHandler(chordsField);
    }

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

        new TablatureInputHandler(tablatureArea);
    }

    private void initLyricsField(Font font) {
        lyricsField = new JTextField();
        lyricsField.setFont(font);
        LengthFilter lyricLengthFilter = new LengthFilter(65);
        ((AbstractDocument) lyricsField.getDocument()).setDocumentFilter(lyricLengthFilter);
        lyricsField.setBorder(new EmptyBorder(0, 30, 0, 0));
    }

    private void layoutComponents(JButton removeButton) {
        int targetWidth = 600;
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

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 5, 0); 
        gbc.anchor = GridBagConstraints.CENTER;
        this.add(chordsField, gbc);

        gbc.gridy = 1; 
        gbc.anchor = GridBagConstraints.CENTER; 
        this.add(tablatureArea, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        gbc.insets = new Insets(0, 15, 5, 0); 
        gbc.anchor = GridBagConstraints.WEST;
        this.add(removeButton, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 15, 0); 
        gbc.anchor = GridBagConstraints.CENTER;
        this.add(lyricsField, gbc);

        Component dummy = Box.createRigidArea(new Dimension(55, 0));
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        this.add(dummy, gbc);
    }

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

    public void setOnRemoveCallback(Consumer<SongLinePanel> callback) {
        this.onRemoveCallback = callback;
    }

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