package controller;

import model.SongLine;
import model.Tablature;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Handles the bidirectional synchronization of data between the SongLine model
 * and the SongLinePanel UI components.
 */
public class SongLineMapper {

    /**
     * Pulls data from the UI fields and updates the provided SongLine model.
     * * @param model             The SongLine model to update.
     * @param chordsField       The UI field containing chords.
     * @param lyricsField       The UI field containing lyrics.
     * @param tablatureArea     The UI area containing tablature.
     * @param sectionLabelField The UI field containing the section label.
     */
    public static void updateModelFromUI(SongLine model, JTextField chordsField, 
                                         JTextField lyricsField, JTextArea tablatureArea, 
                                         JTextField sectionLabelField) {
        if (model == null) return;
        
        model.setChords(chordsField.getText());
        model.setLyrics(lyricsField.getText());
        model.setTablature(Tablature.parseTablature(tablatureArea.getText()));
        model.setSectionLabel(sectionLabelField.getText().trim());
    }

    /**
     * Resets the UI fields to a blank, default state.
     * * @param chordsField       The UI field containing chords.
     * @param lyricsField       The UI field containing lyrics.
     * @param tablatureArea     The UI area containing tablature.
     * @param sectionLabelField The UI field containing the section label.
     */
    public static void clearUIContents(JTextField chordsField, JTextField lyricsField, 
                                       JTextArea tablatureArea, JTextField sectionLabelField) {
        chordsField.setText(" ".repeat(47));
        lyricsField.setText("");
        tablatureArea.setText(new Tablature().toString());
        sectionLabelField.setText("");
    }
}
