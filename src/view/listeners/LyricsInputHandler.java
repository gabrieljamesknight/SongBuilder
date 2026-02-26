package view.listeners;

import java.awt.Font;

import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;

import view.components.LengthFilter;

/**
 * Encapsulates the configuration and input handling for the lyrics text field.
 * Applies visual styling, alignment borders, and document length constraints
 * to ensure the lyrics remain synchronized with the overarching song grid.
 */
public class LyricsInputHandler {

    private final JTextField lyricsField;
    private static final int MAX_LYRIC_LENGTH = 56;
    private static final int LEFT_PADDING = 30;

    /**
     * Constructs the handler and applies the necessary formatting to the target field.
     *
     * @param lyricsField The text field dedicated to lyric input.
     * @param font        The font to be applied to the text field.
     */
    public LyricsInputHandler(JTextField lyricsField, Font font) {
        this.lyricsField = lyricsField;
        configureField(font);
    }

    /**
     * Applies fonts, borders, and document filters to the target text field.
     */
    private void configureField(Font font) {
        lyricsField.setFont(font);
        
        // Apply length constraint to prevent UI stretching
        LengthFilter lyricLengthFilter = new LengthFilter(MAX_LYRIC_LENGTH);
        ((AbstractDocument) lyricsField.getDocument()).setDocumentFilter(lyricLengthFilter);
        
        // Apply padding to align text with the start of the tablature grid
        lyricsField.setBorder(new EmptyBorder(0, LEFT_PADDING, 0, 0));
    }
}