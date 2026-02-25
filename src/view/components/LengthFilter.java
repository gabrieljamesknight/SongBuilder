package view.components;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * A reusable DocumentFilter that restricts the maximum number of characters
 * allowed in a text component. Useful for maintaining strict grid alignments
 * in tablature and chord text fields.
 */
public class LengthFilter extends DocumentFilter {
    private final int max;

    /**
     * Constructs a new LengthFilter.
     * * @param max The maximum number of characters allowed in the document.
     */
    public LengthFilter(int max) { 
        this.max = max; 
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (this.max < 0 || fb.getDocument().getLength() + string.length() <= this.max) {
            super.insertString(fb, offset, string, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if (this.max < 0 || fb.getDocument().getLength() + text.length() - length <= this.max) {
            super.replace(fb, offset, length, text, attrs);
        }
    }
}